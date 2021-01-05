package ru.pushkin.mmb.deezer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.pushkin.mmb.config.ServicePropertyConfig;
import ru.pushkin.mmb.data.SessionsStorage;
import ru.pushkin.mmb.data.enumeration.SessionDataCode;
import ru.pushkin.mmb.deezer.model.Playlist;
import ru.pushkin.mmb.deezer.model.Playlists;
import ru.pushkin.mmb.deezer.model.Track;
import ru.pushkin.mmb.deezer.model.Tracks;
import ru.pushkin.mmb.deezer.model.internal.PlaylistId;
import ru.pushkin.mmb.security.SecurityHelper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DeezerApiServiceImpl implements DeezerApiService {

	private static final Integer PLAYLISTS_DEFAULT_LIMIT = 500;

	private final DeezerApiProvider deezerApiProvider;
	private final ServicePropertyConfig servicePropertyConfig;
	private final SessionsStorage sessionsStorage;
	private ExecutorService executorService;


	@PostConstruct
	public void init() {
		Integer deezerApiServiceThreadPoolSize = servicePropertyConfig.getDeezerApiServiceThreadPoolSize();
		executorService = Executors.newFixedThreadPool(deezerApiServiceThreadPoolSize);
	}

	@PreDestroy
	public void stop() {
		executorService.shutdown();
	}

	@Override
	public String getUserAuthorizationPageUrl() {
		return deezerApiProvider.getUserAuthorizationPageUrl();
	}

	@Override
	public String checkAuthorizationCode(String locationUri) throws DeezerApiErrorException {
		assert locationUri != null;

		if (locationUri.startsWith(DeezerApiConst.DEEZER_API_DEFAULT_REDIRECT_URI)) {
			log.debug("redirect location url detected: {}", locationUri);

			int codeParamStartPosition = locationUri.indexOf(DeezerApiConst.DEEZER_API_AUTH_PARAM_CODE_NAME);
			if (codeParamStartPosition > 0) {
				// append param name length and 1 for character '=' to make substring only for value
				String deezerAppAuthCode = locationUri.substring(
						codeParamStartPosition + DeezerApiConst.DEEZER_API_AUTH_PARAM_CODE_NAME.length() + 1);
				log.info("Deezer authorization code: {}", deezerAppAuthCode);
				return deezerAppAuthCode;
			}

			int errorReasonParamStartPosition = locationUri.indexOf(DeezerApiConst.DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME);
			if (errorReasonParamStartPosition > 0) {
				String errorReason = locationUri.substring(
						errorReasonParamStartPosition + DeezerApiConst.DEEZER_API_AUTH_PARAM_ERROR_REASON_NAME.length() + 1);
				throw new DeezerApiErrorException("Error Reason: " + errorReason);
			}
		}

		return null;
	}

	@Override
	public String obtainNewAccessToken(String code) {
		String userId = SecurityHelper.getUserIdFromToken();
		String accessToken = sessionsStorage.getDeezerAccessToken(userId);
		if (accessToken != null) {
			log.warn("Deezer access token already set in storage, they will be overwritten: userId = {}", userId);
		}

		String newAccessToken = deezerApiProvider.getAccessToken(code);
		if (newAccessToken != null) {
			log.info("Set new Deezer access token: userId = {}", userId);
			sessionsStorage.saveSessionData(SessionDataCode.DEEZER_ACCESS_TOKEN, userId, newAccessToken);
			return newAccessToken;
		} else {
			log.warn("Received empty access token, current user token will not be updated");
			throw new DeezerApiErrorException();
		}
	}

	@Override
	public void getTrack(long trackId) throws DeezerApiErrorException {
		try {
			Track track = deezerApiProvider.getTrack(trackId, getAccessToken());
			log.debug("Received deezer track: {}", track);
		} catch (DeezerApiErrorException e) {
			log.error("Deezer api error:", e);
		}
	}

	@Override
	public List<Playlist> getPlaylists() throws DeezerApiErrorException {
		String currentAccessToken = getAccessToken();
		try {
			// get user playlists
			List<Playlist> playlists = new ArrayList<>();
			Playlists playlistsResponse;
			int index = 0;
			do {
				playlistsResponse = deezerApiProvider.getPlaylists(currentAccessToken, index, PLAYLISTS_DEFAULT_LIMIT);
				playlists.addAll(playlistsResponse.getData());
				index += PLAYLISTS_DEFAULT_LIMIT;
			} while (playlistsResponse.getNext() != null);
			log.debug("Received deezer {} playlists: {}", playlists.size(), playlists);

			// get tracks for playlists
			fetchPlaylistsTracks(playlists, currentAccessToken);
			return playlists;

		} catch (DeezerApiErrorException e) {
			log.error("Deezer api error:", e);
			return new ArrayList<>();
		}
	}

	@Override
	public Playlist getPlaylist(long playlistId) throws DeezerApiErrorException {
		Playlist playlist = deezerApiProvider.getPlaylist(playlistId, getAccessToken());
		log.debug("Received deezer playlist: {}", playlist);
		return playlist;
	}

	private void fetchPlaylistsTracks(List<Playlist> playlists, String currentAccessToken) {
		List<Callable<ImmutablePair<Playlist, List<Track>>>> tasks = playlists.stream()
				.map(playlist -> (Callable<ImmutablePair<Playlist, List<Track>>>) () ->
						ImmutablePair.of(playlist, getPlaylistAllTracks(playlist.getId(), currentAccessToken)))
				.collect(Collectors.toList());

		try {
			List<Future<ImmutablePair<Playlist, List<Track>>>> futures = executorService.invokeAll(tasks);

			// wait until all task will be executed
			while (futures.stream().anyMatch(future -> !future.isDone() && !future.isCancelled())) {
				TimeUnit.MILLISECONDS.sleep(100);
			}

			for (Future<ImmutablePair<Playlist, List<Track>>> future : futures) {
				ImmutablePair<Playlist, List<Track>> entry = future.get();
				Playlist entryPlaylist = entry.getLeft();
				Tracks entryTracks = new Tracks();
				entryTracks.setData(entry.getRight());
				entryPlaylist.setTracks(entryTracks);
			}

		} catch (InterruptedException | ExecutionException e) {
			log.error("Executor Service error", e);
		}

		log.debug("Deezer fetching playlists tracks end");
	}

	private List<Track> getPlaylistAllTracks(long playlistId, String currentAccessToken) {
		List<Track> playlistTracks = new ArrayList<>();
		Tracks tracksResponse;
		int j = 0;
		do {
			try {
				tracksResponse = deezerApiProvider.getPlaylistTracks(playlistId, currentAccessToken, j, PLAYLISTS_DEFAULT_LIMIT);
				playlistTracks.addAll(tracksResponse.getData());
				j += PLAYLISTS_DEFAULT_LIMIT;
			} catch (DeezerApiErrorException e) {
				log.error("Deezer api error:", e);
				break;
			}
		} while (tracksResponse.getNext() != null);

		log.debug("Received deezer playlist tracks: playlist = {}, size = {}", playlistId, playlistTracks.size());
		return playlistTracks;
	}

	@Override
	public List<Track> getPlaylistTracks(long playlistId) throws DeezerApiErrorException {
		return getPlaylistAllTracks(playlistId, getAccessToken());
	}

	@Override
	public Long createPlaylist(String title) throws DeezerApiErrorException {
		PlaylistId playlistId = deezerApiProvider.createPlaylist(title, getAccessToken());
		return (playlistId != null) ? playlistId.getId() : null;
	}

	@Override
	public boolean deletePlaylist(long playlistId) throws DeezerApiErrorException {
		return deezerApiProvider.deletePlaylist(playlistId, getAccessToken());
	}

	@Override
	public boolean renamePlaylist(long playlistId, String newTitle) throws DeezerApiErrorException {
		return deezerApiProvider.renamePlaylist(playlistId, newTitle, getAccessToken());
	}

	@Override
	public boolean addTrackToPlaylist(long playlistId, long trackId) throws DeezerApiErrorException {
		return addTracksToPlaylist(playlistId, Collections.singletonList(trackId));
	}

	@Override
	public boolean addTracksToPlaylist(long playlistId, List<Long> trackIds) throws DeezerApiErrorException {
		return deezerApiProvider.addTracksToPlaylist(playlistId, trackIds, getAccessToken());
	}

	@Override
	public boolean removeTrackFromPlaylist(long playlistId, long trackId) throws DeezerApiErrorException {
		return removeTracksFromPlaylist(playlistId, Collections.singletonList(trackId));
	}

	@Override
	public boolean removeTracksFromPlaylist(long playlistId, List<Long> trackIds) throws DeezerApiErrorException {
		return deezerApiProvider.removeTracksFromPlaylist(playlistId, trackIds, getAccessToken());
	}

	@Override
	public List<Track> searchTracksQuery(String shortQuery, String extendedQuery) throws DeezerApiErrorException {
		String currentAccessToken = getAccessToken();

		Tracks tracksResult = null;
		if (extendedQuery != null) {
			tracksResult = deezerApiProvider.searchTracksQuery(extendedQuery, currentAccessToken);
		}
		if ((tracksResult == null) || CollectionUtils.isEmpty(tracksResult.getData())) {
			tracksResult = deezerApiProvider.searchTracksQuery(shortQuery, currentAccessToken);
		}

		if ((tracksResult != null) && !CollectionUtils.isEmpty(tracksResult.getData())) {
			return tracksResult.getData();
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public List<Track> getFavoriteTracks(int page, int limit) {
		return deezerApiProvider.getFavoriteTracks(getAccessToken(), page, limit).getData();
	}


	@Override
	public boolean addTrackToFavorites(long trackId) throws DeezerApiErrorException {
		return deezerApiProvider.addTrackToFavorites(trackId, getAccessToken());
	}

	@Override
	public boolean removeTrackFromFavorites(long trackId) throws DeezerApiErrorException {
		return deezerApiProvider.removeTrackFromFavorites(trackId, getAccessToken());
	}

	@Override
	public String getDeezerPlaylistWebPageUrl(long playlistId) {
		return String.format(DeezerApiConst.DEEZER_PLAYLIST_PAGE_URL_PATTER, playlistId);
	}

	@Override
	public String getAccessToken() throws DeezerApiErrorException {
		String userId = SecurityHelper.getUserIdFromToken();
		String accessToken = sessionsStorage.getDeezerAccessToken(userId);
		if (accessToken == null) {
			throw new SecurityException("Deezer access token not defined.");
		}
		return accessToken;
	}

}

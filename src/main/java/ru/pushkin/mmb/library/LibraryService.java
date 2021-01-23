package ru.pushkin.mmb.library;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.api.output.dto.PlaylistDto;
import ru.pushkin.mmb.api.output.dto.PlaylistShortDto;
import ru.pushkin.mmb.api.output.dto.TrackDto;
import ru.pushkin.mmb.api.output.enumeration.PlaylistsFilterParam;
import ru.pushkin.mmb.api.output.response.FavoriteTracksResponse;
import ru.pushkin.mmb.api.output.response.ListeningHistoryResponse;
import ru.pushkin.mmb.api.output.response.PlaylistListResponse;
import ru.pushkin.mmb.api.output.response.PlaylistResponse;
import ru.pushkin.mmb.config.ServicePropertyConfig;
import ru.pushkin.mmb.data.Pageable;
import ru.pushkin.mmb.data.enumeration.PlaylistType;
import ru.pushkin.mmb.data.model.library.PlaylistData;
import ru.pushkin.mmb.data.model.library.PlaylistTrack;
import ru.pushkin.mmb.data.model.library.TrackData;
import ru.pushkin.mmb.data.repository.PlaylistDataRepository;
import ru.pushkin.mmb.data.repository.TagDataRepository;
import ru.pushkin.mmb.data.repository.TrackDataRepository;
import ru.pushkin.mmb.data.repository.UserTrackInfoRepository;
import ru.pushkin.mmb.deezer.DeezerApiService;
import ru.pushkin.mmb.exception.PlaylistNotFoundException;
import ru.pushkin.mmb.lastfm.LastFmService;
import ru.pushkin.mmb.mapper.PlaylistDataMapper;
import ru.pushkin.mmb.mapper.TrackDataMapper;
import ru.pushkin.mmb.security.SecurityHelper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LibraryService {

    private final ServicePropertyConfig servicePropertyConfig;
    private final DeezerApiService deezerApiService;
    private final LastFmService lastFmService;
    private final TrackDataMapper trackDataMapper;
    private final PlaylistDataMapper playlistDataMapper;
    private final TrackDataRepository trackDataRepository;
    private final TagDataRepository tagDataRepository;
    private final UserTrackInfoRepository userTrackInfoRepository;
    private final PlaylistDataRepository playlistDataRepository;

    private ExecutorService executorService;


    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(servicePropertyConfig.getLastFm().getServiceThreadPoolSize());
    }

    @PreDestroy
    public void stop() {
        executorService.shutdown();
    }


    /**
     *
     */
    public FavoriteTracksResponse findFavoriteTracks(Integer page, Integer size) {
        String userId = SecurityHelper.getUserIdFromToken();
//        List<Track> deezerFavoriteTracks = deezerApiService.getFavoriteTracks(page, size);
        Pageable<TrackData> favoriteTracks = lastFmService.getFavoriteTracks(userId, page, size);
        List<TrackData> tracksData = fillTrackData(favoriteTracks.getData(), userId);
        List<TrackDto> trackDtos = tracksData.stream()
                .map(trackDataMapper::map)
                .collect(Collectors.toList());
        return new FavoriteTracksResponse((int) favoriteTracks.getPage(), tracksData.size(), favoriteTracks.getTotalSize(), trackDtos);
    }

    /**
     *
     */
    public ListeningHistoryResponse getUserListeningHistory(Integer page, Integer size, LocalDateTime from, LocalDateTime to) {
        Pageable<TrackData> response = loadTrackDataForUserListeningHistory(
                SecurityHelper.getUserIdFromToken(), page, size, from, to
        );
        List<TrackDto> trackDtos = response.getData().stream()
                .map(trackDataMapper::map)
                .collect(Collectors.toList());
        return new ListeningHistoryResponse(page, size, response.getTotalSize(), trackDtos);
    }

    /**
     *
     */
    public Pageable<TrackData> loadTrackDataForUserListeningHistory(
            @NotNull String userId, @NotNull Integer page, @NotNull Integer size,
            LocalDateTime from, LocalDateTime to
    ) {
        Date dateFrom = from != null ? Date.from(from.toInstant(ZoneOffset.UTC)) : null;
        Date dateTo = to != null ? Date.from(to.toInstant(ZoneOffset.UTC)) : null;
        Pageable<TrackData> recentTracks = lastFmService.fetchRecentTracks(userId, page, size, dateFrom, dateTo);
        List<TrackData> tracksData = fillTrackData(recentTracks.getData(), userId);
        return new Pageable<>(recentTracks.getPage(), tracksData.size(), recentTracks.getTotalPages(), recentTracks.getTotalSize(), tracksData);
    }

    private List<TrackData> fillTrackData(Collection<TrackData> tracks, String userId) {
        ConcurrentMap<String, TrackData> tracksStore = new ConcurrentHashMap<>(
                fetchTracksMapByMbidOrTitle(tracks, userId)
        );

        List<Callable<TrackData>> tasks = tracks.stream()
                .map(track -> (Callable<TrackData>) () -> {
                    TrackData storedTrack = tracksStore.get(track.getTitle());
                    if (storedTrack == null) {
                        log.debug("Begin fetch track data (track = {})", track);
                        TrackData trackData = lastFmService.fillTrackDataInfo(track, userId);

                        log.debug("End fetch track data (track = {})", track);
                        tracksStore.putIfAbsent(track.getTitle(), trackData);
                        return trackData;
                    } else {
                        // copy transient fields
                        storedTrack.setDateTime(track.getDateTime());
                        storedTrack.setUserTrackData(track.getUserTrackData());
                    }
                    return storedTrack;
                }).collect(Collectors.toList());

        List<TrackData> tracksData = new ArrayList<>();
        try {
            List<Future<TrackData>> futures = executorService.invokeAll(tasks);

            // wait until all task will be executed
            while (futures.stream().anyMatch(future -> !future.isDone() && !future.isCancelled())) {
                TimeUnit.MILLISECONDS.sleep(100);
            }

            for (Future<TrackData> future : futures) {
                try {
                    tracksData.add(future.get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Executor Service error", e);
                }
            }

        } catch (InterruptedException e) {
            log.error("Executor Service error", e);
        }

        log.debug("Fetch tracks data end (tracks data size: {})", tracksData.size());
        return tracksData;
    }

    /**
     * @return fetched playlists count
     */
    public long loadTrackDataForUserListeningHistory(@NotNull String userId, @NotNull LocalDateTime from, @NotNull LocalDateTime to) {
        int pageSize = 200;

        log.debug("Start fetch listening history for user (userId: {}, from: {}, to: {}, page size: {})", userId, from, to, pageSize);

        long totalPages = 1;
        long fetchedSize = 0;
        for (int page = 0; page < totalPages; page++) {
            Pageable<TrackData> response = loadTrackDataForUserListeningHistory(userId, page, pageSize, from, to);
            totalPages = response.getTotalPages();
            long totalSize = response.getTotalSize();
            long responseSize = response.getSize();
            fetchedSize += responseSize;
            log.debug("Fetched page {} of {}, Page size = {}, Fetched data size {} of {}",
                    page, totalPages - 1, responseSize, fetchedSize, totalSize);
            if (responseSize <= 0) {
                break;
            }
        }

        log.debug("Finish fetch listening history for user (userId: {}, from: {}, to: {}, fetched size: {})",
                userId, from, to, fetchedSize);

        return fetchedSize;
    }

    /**
     * @return fetched playlists count
     */
    @Transactional
    public int loadPlaylistsForUserFromDeezer() {
        String userId = SecurityHelper.getUserIdFromToken();
        log.debug("Start fetch playlists from Deezer for user (userId: {})", userId);

        List<PlaylistData> playlists = deezerApiService.getPlaylists();
        int totalPlaylists = playlists.size();
        log.debug("Obtained playlists from Deezer for user (userId: {}, count: {})", userId, totalPlaylists);

        Map<String, PlaylistData> playlistsMap = playlists.stream().collect(Collectors.toMap(PlaylistData::getSourceUrl, p -> p));
        List<PlaylistData> existedPlaylists = playlistDataRepository.findBySourceUrlInAndUserIdAndType(playlistsMap.keySet(),
                userId, PlaylistType.DEEZER);
        log.debug("Existed Deezer playlists fetched (userId: {}, count: {})", userId, existedPlaylists.size());
        existedPlaylists.forEach(p -> playlistsMap.remove(p.getSourceUrl()));

        playlists = playlistsMap.values().stream().collect(Collectors.toList());
        for (PlaylistData playlist : playlists) {
            Set<TrackData> trackSet = new HashSet<>();
            Map<String, List<PlaylistTrack>> tracksMap = new HashMap<>();
            for (PlaylistTrack playlistTrack : playlist.getTracks()) {
                String key = playlistTrack.getTrackData().getTitle();
                if (!tracksMap.containsKey(key)) {
                    tracksMap.put(key, new ArrayList<>());
                }
                tracksMap.get(key).add(playlistTrack);
                trackSet.add(playlistTrack.getTrackData());
            }
            List<TrackData> tracksData = fillTrackData(trackSet, userId);
            tracksData.forEach(trackData -> tracksMap.get(trackData.getTitle())
                    .forEach(playlistTrack -> playlistTrack.setTrackData(trackData)));
            log.debug("Fetched data for playlist {} of {}", playlists.indexOf(playlist), totalPlaylists - 1);
        }
        List<PlaylistData> savedPlaylists = playlistDataRepository.saveAll(playlists);

        log.debug("Finish fetch playlists from Deezer for user (userId: {}, fetched size: {})",
                userId, savedPlaylists.size());
        return savedPlaylists.size();
    }

    /**
     *
     */
    public PlaylistListResponse getPlaylistList(int page, int size, PlaylistsFilterParam filter) {
        String userId = SecurityHelper.getUserIdFromToken();
        log.debug("Start getPlaylistList (userId: {})", userId);

        Page<PlaylistData> playlistsPage = new PageImpl<>(List.of());
        PageRequest pageable = PageRequest.of(page, size);
        if (PlaylistsFilterParam.DEEZER.equals(filter)) {
            playlistsPage = playlistDataRepository.findByUserIdAndType(userId, PlaylistType.DEEZER, pageable);
        } else if (PlaylistsFilterParam.ALL.equals(filter)){
            playlistsPage = playlistDataRepository.findByUserId(userId, pageable);
        }

        List<PlaylistShortDto> playlistDtos = playlistsPage.getContent().stream()
                .map(playlistDataMapper::mapShort)
                .collect(Collectors.toList());

        log.debug("Finish getPlaylistList (userId: {})", userId);
        return new PlaylistListResponse(page, size, playlistsPage.getTotalElements(), playlistDtos);
    }

    public PlaylistResponse getPlaylist(int playlistId) throws PlaylistNotFoundException {
        String userId = SecurityHelper.getUserIdFromToken();
        log.debug("Start getPlaylists (userId: {}, playlistId: {})", userId, playlistId);
        PlaylistData playlistData = playlistDataRepository.findByIdAndUserId(playlistId, userId);
        if (playlistData == null) {
            throw new PlaylistNotFoundException(playlistId);
        }

        PlaylistDto playlistDto = playlistDataMapper.map(playlistData);
        return new PlaylistResponse(playlistDto);
    }


    private Map<String, TrackData> fetchTracksMapByMbidOrTitle(Collection<TrackData> trackDatas, String userId) {
        List<String> mbids = trackDatas.stream()
                .map(TrackData::getMbid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> titles = trackDatas.stream()
                .map(TrackData::getTitle)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return trackDataRepository.findAllByMbidOrTitle(mbids, titles, userId).stream()
                .collect(Collectors.toMap(TrackData::getTitle, o -> o));
    }
}

package ru.pushkin.mmb.lastfm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.config.ServicePropertyConfig;
import ru.pushkin.mmb.data.Pageable;
import ru.pushkin.mmb.data.SessionsStorage;
import ru.pushkin.mmb.data.enumeration.SessionDataCode;
import ru.pushkin.mmb.data.model.library.TrackData;
import ru.pushkin.mmb.data.repository.TrackDataRepository;
import ru.pushkin.mmb.lastfm.model.*;
import ru.pushkin.mmb.mapper.TrackDataMapper;
import ru.pushkin.mmb.security.SecurityHelper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.transaction.Transactional;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LastFmService {
    private static final String LASTFM_API_AUTH_BASE_URL = "https://www.last.fm/api/auth?api_key=%s&cb=%s";
    private static final String DUPLICATE_ENTITY_ERROR_SQL_STATE = "23505";

    private final ServicePropertyConfig servicePropertyConfig;
    private final LastFmApiProvider lastFmApiProvider;
    private final SessionsStorage sessionsStorage;
    private final TrackDataMapper trackDataMapper;
    private final TrackDataRepository trackDataRepository;
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
     * See https://www.last.fm/api/webauth
     */
    public String formUserAuthorizationPageUrl() {
        return String.format(LASTFM_API_AUTH_BASE_URL, servicePropertyConfig.getLastFm().getApplicationApiKey(),
                servicePropertyConfig.getLastFm().getRedirectUrl());
    }

    /**
     * See https://www.last.fm/api/webauth
     */
    public String obtainNewSessionKey(String token, String username) {
        String userId = SecurityHelper.getUserIdFromToken();
        String lastFmSessionKey = sessionsStorage.getLastFmSessionKey(userId);
        if (lastFmSessionKey != null) {
            log.warn("LastFm session already set in storage, they will be overwritten: userId = {}", userId);
        }

        Optional<String> sessionKey = lastFmApiProvider.authGetSession(token);
        if (sessionKey.isPresent()) {
            log.info("Set new LastFm session: userId = {}", userId);
            sessionsStorage.saveSessionData(SessionDataCode.LAST_FM_SESSION_KEY, userId, sessionKey.get());
            Optional<User> user = lastFmApiProvider.userGetInfo(username);
            user.ifPresent(u ->
                    sessionsStorage.saveSessionData(SessionDataCode.LAST_FM_USERNAME, userId, u.getName())
            );
            return sessionKey.get();
        } else {
            log.warn("Received empty session key, current user session will not be updated");
            throw new LastFmApiErrorException();
        }
    }

    /**
     * Get LastFm session key from storage.
     */
    public String getSessionKey() {
        String userId = SecurityHelper.getUserIdFromToken();
        String sessionKey = sessionsStorage.getLastFmSessionKey(userId);
        if (sessionKey == null) {
            log.error("Session key not defined: userId = {}", userId);
            throw new SecurityException("LastFm session key not defined.");
        }
        return sessionKey;
    }

    public Optional<LovedTracks> getFavoriteTracks(String userId, Integer page, Integer limit) {
        String lastFmUsername = sessionsStorage.getLastFmUsername(userId);
        return lastFmApiProvider.userGetLovedTracks(lastFmUsername, page, limit);

    }

    @Transactional
    public Pageable<TrackData> fetchRecentTracks(String userId, Integer page, Integer limit, Date from, Date to) {
        String lastFmUsername = sessionsStorage.getLastFmUsername(userId);
        return lastFmApiProvider.userGetRecentTracks(lastFmUsername, page, limit, from, to, true)
                .map(o -> {
                    List<TrackData> tracks = o.getTracks().stream()
                            .map(trackDataMapper::mapTrackData)
                            .collect(Collectors.toList());
                    tracks = fetchTracksData(tracks, lastFmUsername);
                    return new Pageable<>(o.getPage(), o.getPerPage(), o.getTotalPages(), o.getTotal(), tracks);
                })
                .orElse(Pageable.empty());
    }

    private List<TrackData> fetchTracksData(List<TrackData> tracks, String lastFmUsername) {
        log.debug("Fetch tracks data start");

        ConcurrentMap<String, TrackData> tracksStore = fetchTracksMapByMbidOrTitle(tracks);

        List<Callable<TrackData>> tasks = tracks.stream()
                .map(track -> (Callable<TrackData>) () -> {
                    TrackData storedTrack = tracksStore.get(track.getTitle());
                    if (storedTrack != null) {
                        track.setLength(storedTrack.getLength());
                        track.setMbid(storedTrack.getMbid());
                        return track;
                    } else {
                        log.debug("Begin fetch track data (track = {})", track);
                        TrackData trackData = fetchTrackData(track, lastFmUsername);
                        log.debug("End fetch track data (track = {})", track);
                        tracksStore.putIfAbsent(track.getTitle(), trackData);
                        return trackData;
                    }
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

    private TrackData fetchTrackData(TrackData track, String lastFmUsername) {
        TrackData trackData = track;
        TrackInfo trackInfo = lastFmApiProvider.trackGetInfo(null, track.getTrackName(), track.getArtist(), lastFmUsername, false)
                .orElse(null);

        if (trackInfo != null) {
            Optional<TrackData> foundedTrack = StringUtils.isNotEmpty(trackInfo.getMbid()) ?
                    trackDataRepository.findByMbidOrTitle(trackInfo.getMbid(), track.getTitle()) :
                    trackDataRepository.findByTitle(trackData.getTitle());
            if (foundedTrack.isPresent()) {
                trackData = foundedTrack.get();
            }
            if (trackData.getLength() == null) {
                trackData.setLength(trackInfo.getDuration());
            }
            if (StringUtils.isEmpty(trackData.getMbid())) {
                trackData.setMbid(trackInfo.getMbid());
            }
            if (StringUtils.isEmpty(trackData.getAlbum()) && trackInfo.getAlbum() != null) {
                trackData.setAlbum(trackInfo.getAlbum().getTitle());
            }
        }

        return trackDataRepository.save(trackData);
    }

    private ConcurrentMap<String, TrackData> fetchTracksMapByMbidOrTitle(List<TrackData> trackDatas) {
        List<String> mbids = trackDatas.stream()
                .map(TrackData::getMbid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> titles = trackDatas.stream()
                .map(TrackData::getTitle)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<String, TrackData> tracksMap = trackDataRepository.findAllByMbidInOrTitleIn(mbids, titles).stream()
                .collect(Collectors.toMap(TrackData::getTitle, o -> o));
        return new ConcurrentHashMap<>(tracksMap);
    }

}

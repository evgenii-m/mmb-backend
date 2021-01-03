package ru.pushkin.mmb.lastfm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LastFmService {
    private static final String LASTFM_API_AUTH_BASE_URL = "https://www.last.fm/api/auth?api_key=%s&cb=%s";

    private final ServicePropertyConfig servicePropertyConfig;
    private final LastFmApiProvider lastFmApiProvider;
    private final SessionsStorage sessionsStorage;
    private final TrackDataMapper trackDataMapper;
    private final TrackDataRepository trackDataRepository;

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

    public Optional<LovedTracks> getFavoriteTracks(Integer page, Integer limit) {
        String userId = SecurityHelper.getUserIdFromToken();
        String lastFmUsername = sessionsStorage.getLastFmUsername(userId);
        return lastFmApiProvider.userGetLovedTracks(lastFmUsername, page, limit);

    }

    public Pageable<TrackData> fetchRecentTracks(Integer page, Integer limit, Date from, Date to) {
        String userId = SecurityHelper.getUserIdFromToken();
        String lastFmUsername = sessionsStorage.getLastFmUsername(userId);
        return lastFmApiProvider.userGetRecentTracks(lastFmUsername, page, limit, from, to, true)
                .map(o -> {
                    List<TrackData> tracks = o.getTracks().stream()
                            .map(trackDataMapper::mapTrackData)
                            .collect(Collectors.toList());
                    Map<String, TrackData> tracksStore = fetchTracksMapByMbidOrTitle(tracks);
                    tracks = tracks.stream().map(track -> {
                        TrackData storedTrack = tracksStore.get(track.getTitle());
                        if (storedTrack != null) {
                            track.setLength(storedTrack.getLength());
                            track.setMbid(storedTrack.getMbid());
                            return track;
                        } else  {
                            lastFmApiProvider.trackGetInfo(null, track.getTrackName(), track.getArtist(), lastFmUsername, false)
                                    .ifPresent(info -> {
                                        track.setLength(info.getDuration());
                                        track.setMbid(info.getMbid());
                                    });
                            return trackDataRepository.save(track);
                        }
                    }).collect(Collectors.toList());
                    return new Pageable<>(page, limit, o.getTotal(), tracks);
                })
                .orElse(Pageable.empty());
    }

    private Map<String, TrackData> fetchTracksMapByMbidOrTitle(List<TrackData> trackDatas) {
        List<String> mbids = trackDatas.stream()
                .map(TrackData::getMbid)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> titles = trackDatas.stream()
                .map(TrackData::getTitle)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return trackDataRepository.findAllByMbidInOrTitleIn(mbids, titles).stream()
                .collect(Collectors.toMap(TrackData::getTitle, o -> o));
    }

}

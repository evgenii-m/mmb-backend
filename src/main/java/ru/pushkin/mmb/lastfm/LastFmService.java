package ru.pushkin.mmb.lastfm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.pushkin.mmb.config.ServicePropertyConfig;
import ru.pushkin.mmb.data.Pageable;
import ru.pushkin.mmb.data.SessionsStorage;
import ru.pushkin.mmb.data.enumeration.SessionDataCode;
import ru.pushkin.mmb.data.model.library.TagData;
import ru.pushkin.mmb.data.model.library.TrackData;
import ru.pushkin.mmb.data.model.library.UserTrackData;
import ru.pushkin.mmb.data.repository.TagDataRepository;
import ru.pushkin.mmb.data.repository.TrackDataRepository;
import ru.pushkin.mmb.data.repository.UserTrackDataRepository;
import ru.pushkin.mmb.lastfm.model.*;
import ru.pushkin.mmb.mapper.TrackDataMapper;
import ru.pushkin.mmb.security.SecurityHelper;

import javax.annotation.PostConstruct;
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

    private final ServicePropertyConfig servicePropertyConfig;
    private final LastFmApiProvider lastFmApiProvider;
    private final SessionsStorage sessionsStorage;
    private final TrackDataMapper trackDataMapper;
    private final TrackDataRepository trackDataRepository;
    private final TagDataRepository tagDataRepository;
    private final UserTrackDataRepository userTrackDataRepository;

    @Autowired
    private LastFmService self;

    private ConcurrentMap<String, TagData> tagDataCache;


    @PostConstruct
    public void init() {
        tagDataCache = new ConcurrentHashMap<>(
                tagDataRepository.findAll().stream()
                        .collect(Collectors.toMap(TagData::getName, o -> o))
        );
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

    public Pageable<TrackData> getFavoriteTracks(String userId, Integer page, Integer limit) {
        String lastFmUsername = sessionsStorage.getLastFmUsername(userId);
        return lastFmApiProvider.userGetLovedTracks(lastFmUsername, page, limit)
                .map(o -> {
                    List<TrackData> tracks = o.getTracks().stream()
                            .map(trackDataMapper::map)
                            .collect(Collectors.toList());
                    return new Pageable<>(o.getPage(), o.getPerPage(), o.getTotalPages(), o.getTotal(), tracks);
                })
                .orElse(Pageable.empty());

    }

    public Pageable<TrackData> fetchRecentTracks(String userId, Integer page, Integer limit, Date from, Date to) {
        String lastFmUsername = sessionsStorage.getLastFmUsername(userId);
        return lastFmApiProvider.userGetRecentTracks(lastFmUsername, page, limit, from, to, true)
                .map(o -> {
                    List<TrackData> tracks = o.getTracks().stream()
                            .map(trackDataMapper::map)
                            .collect(Collectors.toList());
                    return new Pageable<>(o.getPage(), o.getPerPage(), o.getTotalPages(), o.getTotal(), tracks);
                })
                .orElse(Pageable.empty());
    }

    public TrackData fillTrackDataInfo(TrackData track, String userId) {
        String lastFmUsername = sessionsStorage.getLastFmUsername(userId);
        TrackInfo trackInfo = lastFmApiProvider.trackGetInfo(null, track.getTrackName(), track.getArtist(), lastFmUsername, false)
                .orElse(null);
        return self.fetchTrackData(track, trackInfo, userId);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    synchronized TrackData fetchTrackData(TrackData track, TrackInfo trackInfo, String userId) {
        TrackData trackData = track;
        if (trackInfo != null) {
            if (trackData.getId() == null) {
                Optional<TrackData> foundedTrack = StringUtils.isNotEmpty(trackInfo.getMbid()) ?
                        trackDataRepository.findByMbidOrTitle(trackInfo.getMbid(), track.getTitle(), userId) :
                        trackDataRepository.findByTitle(trackData.getTitle(), userId);
                if (foundedTrack.isPresent()) {
                    trackData = foundedTrack.get();
                    trackData.setDateTime(track.getDateTime());
                    trackData.setTitle(track.getTitle());
                }
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
            if (StringUtils.isEmpty(trackData.getLastFmUrl()) && StringUtils.isNotEmpty(trackInfo.getUrl())) {
                trackData.setLastFmUrl(trackInfo.getUrl());
            }

            Set<TagData> tags = self.fetchTagData(trackInfo.getTopTags());
            trackData.setTags(tags);

            trackData = trackDataRepository.save(trackData);

            UserTrackData userTrackData = self.fetchUserInfo(trackInfo, trackData.getId(), userId);
            trackData.setUserTrackData(userTrackData);

        } else {
            trackData = trackDataRepository.save(trackData);
        }

        return trackData;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    synchronized Set<TagData> fetchTagData(TopTags topTags) {
        Set<TagData> tags = new HashSet<>();
        if (topTags != null && !CollectionUtils.isEmpty(topTags.getTags())) {
            Set<String> tagNames = topTags.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
            Map<String, TagData> existedTagsMap = tagDataRepository.findByNameIn(tagNames).stream()
                    .collect(Collectors.toMap(TagData::getName, t -> t));
            Set<TagData> newTags = new HashSet<>();
            for (Tag tag : topTags.getTags()) {
                TagData tagData = existedTagsMap.get(tag.getName());
                if (tagData == null) {
                    TagData newTagData = new TagData(tag.getName(), tag.getUrl());
                    newTags.add(newTagData);
                    tagData = newTagData;
                }
                tags.add(tagData);
            }
            tagDataRepository.saveAll(newTags);
        }
        return tags;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    synchronized UserTrackData fetchUserInfo(TrackInfo trackInfo, Integer trackId, String userId) {
        UserTrackData userTrackData = null;
        if (trackInfo != null) {
            Optional<UserTrackData> storedUserInfo = userTrackDataRepository.findByTrackIdAndUserId(trackId, userId);
            if (storedUserInfo.isPresent()) {
                userTrackData = storedUserInfo.get();
            } else {
                userTrackData = new UserTrackData();
                userTrackData.setUserId(userId);
                userTrackData.setTrackId(trackId);
            }
            userTrackData.setFavorite(trackInfo.getUserloved());
            userTrackData.setListenCount(trackInfo.getUserplaycount());

            userTrackData = userTrackDataRepository.save(userTrackData);
        }

        return userTrackData;
    }
}

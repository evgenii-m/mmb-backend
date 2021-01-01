package ru.pushkin.mmb.lastfm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.config.ServicePropertyConfig;
import ru.pushkin.mmb.data.SessionsStorage;
import ru.pushkin.mmb.data.enumeration.SessionDataCode;
import ru.pushkin.mmb.lastfm.model.LovedTracks;
import ru.pushkin.mmb.lastfm.model.TrackInfo;
import ru.pushkin.mmb.lastfm.model.User;
import ru.pushkin.mmb.security.SecurityHelper;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class LastFmService {
    private static final String LASTFM_API_AUTH_BASE_URL = "https://www.last.fm/api/auth?api_key=%s&cb=%s";

    private final ServicePropertyConfig servicePropertyConfig;
    private final LastFmApiProvider lastFmApiProvider;
    private final SessionsStorage sessionsStorage;

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
            if (user.isPresent()) {
                sessionsStorage.saveSessionData(SessionDataCode.LAST_FM_USERNAME, userId, user.get().getName());
            }
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

    public List<TrackInfo> getFavoriteTracks(int page, int limit) {
        String userId = SecurityHelper.getUserIdFromToken();
        String lastFmUsername = sessionsStorage.getLastFmUsername(userId);
        return lastFmApiProvider.userGetLovedTracks(lastFmUsername, page + 1, limit)
                .map(LovedTracks::getTracks)
                .orElse(List.of());

    }
}

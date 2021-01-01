package ru.pushkin.mmb.data;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Component;
import ru.pushkin.mmb.data.enumeration.SessionDataCode;
import ru.pushkin.mmb.data.model.SessionData;
import ru.pushkin.mmb.data.repository.SessionDataRepository;

import java.util.Base64;

@RequiredArgsConstructor
@Component
public class SessionsStorage {

    private final SessionDataRepository sessionDataRepository;
    private final BytesEncryptor standardEncryptor;

    public String getDeezerAccessToken(String userId) {
        return fetchAndDecryptSessionData(userId, SessionDataCode.DEEZER_ACCESS_TOKEN);
    }

    public String getLastFmSessionKey(String userId) {
        return fetchAndDecryptSessionData(userId, SessionDataCode.LAST_FM_SESSION_KEY);
    }

    public String getLastFmUsername(String userId) {
        return fetchAndDecryptSessionData(userId, SessionDataCode.LAST_FM_USERNAME);
    }

    private String fetchAndDecryptSessionData(String userId, SessionDataCode sessionDataCode) {
        return sessionDataRepository.findByCodeAndUserId(sessionDataCode.name(), userId).stream()
                .findFirst()
                .map(SessionData::getValue)
                .map(value -> new String(standardEncryptor.decrypt(Base64.getDecoder().decode(value))))
                .orElse(null);
    }

    public void saveSessionData(SessionDataCode sessionDataCode, String userId, String data) {
        String encryptedBase64 = Base64.getEncoder().encodeToString(
                standardEncryptor.encrypt(data.getBytes())
        );
        SessionData sessionData = new SessionData(sessionDataCode.name(), encryptedBase64, userId);
        sessionDataRepository.save(sessionData);
    }

}

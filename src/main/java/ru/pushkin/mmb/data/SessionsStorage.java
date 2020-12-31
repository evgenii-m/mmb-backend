package ru.pushkin.mmb.data;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Component;
import ru.pushkin.mmb.data.enumeration.SessionTypeCode;
import ru.pushkin.mmb.data.model.SessionData;
import ru.pushkin.mmb.data.repository.SessionDataRepository;

import java.util.Base64;

@RequiredArgsConstructor
@Component
public class SessionsStorage {

    private final SessionDataRepository sessionDataRepository;
    private final BytesEncryptor standardEncryptor;

    public String getDeezerAccessToken(String userId) {
        return sessionDataRepository.findByCodeAndUserId(SessionTypeCode.DEEZER_ACCESS_TOKEN.name(), userId).stream()
                .findFirst()
                .map(SessionData::getValue)
                .map(accessToken -> new String(standardEncryptor.decrypt(Base64.getDecoder().decode(accessToken))))
                .orElse(null);
    }


    public String getLastFmSessionKey(String userId) {
        return sessionDataRepository.findByCodeAndUserId(SessionTypeCode.LAST_FM_SESSION_KEY.name(), userId).stream()
                .findFirst()
                .map(SessionData::getValue)
                .map(sessionKey -> new String(standardEncryptor.decrypt(Base64.getDecoder().decode(sessionKey))))
                .orElse(null);
    }

    public void saveSessionData(SessionTypeCode sessionTypeCode, String userId, String data) {
        String encryptedBase64 = Base64.getEncoder().encodeToString(
                standardEncryptor.encrypt(data.getBytes())
        );
        SessionData sessionData = new SessionData(sessionTypeCode.name(), encryptedBase64, userId);
        sessionDataRepository.save(sessionData);
    }

}

package ru.pushkin.mmb.data;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.pushkin.mmb.config.JwtPropertyConfig;
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


    public void saveDeezerAccessToken(String userId, String currentAccessToken) {
        String encryptedTokenBase64 = Base64.getEncoder().encodeToString(
                standardEncryptor.encrypt(currentAccessToken.getBytes())
        );
        SessionData sessionData = new SessionData(SessionTypeCode.DEEZER_ACCESS_TOKEN.name(), encryptedTokenBase64, userId);
        sessionDataRepository.save(sessionData);
    }

}

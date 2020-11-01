package ru.pushkin.mma.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.pushkin.mma.data.enumeration.SessionTypeCode;
import ru.pushkin.mma.data.model.SessionData;
import ru.pushkin.mma.data.repository.SessionDataRepository;
import ru.pushkin.mma.security.SecurityHelper;

@Component
public class SessionsStorage {

    @Autowired
    private SessionDataRepository sessionDataRepository;


    public String getDeezerAccessToken(String userId) {
        return sessionDataRepository.findByCodeAndUserId(SessionTypeCode.DEEZER_ACCESS_TOKEN.name(), userId).stream()
                .findFirst()
                .map(SessionData::getValue)
                .orElse(null);
    }


    public void saveDeezerAccessToken(String userId, String currentAccessToken) {
        SessionData sessionData = new SessionData(SessionTypeCode.DEEZER_ACCESS_TOKEN.name(), currentAccessToken, userId);
        sessionDataRepository.save(sessionData);
    }
}

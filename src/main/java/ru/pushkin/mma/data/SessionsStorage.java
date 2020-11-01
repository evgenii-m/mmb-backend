package ru.pushkin.mma.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.pushkin.mma.data.enumeration.SessionTypeCode;
import ru.pushkin.mma.data.model.SessionData;
import ru.pushkin.mma.data.repository.SessionDataRepository;

@Component
public class SessionsStorage {

    private static final String TEST_USER_ID = "user-1";

    @Autowired
    private SessionDataRepository sessionDataRepository;


    public String getDeezerAccessToken() {
        return sessionDataRepository.findByCodeAndUserId(SessionTypeCode.DEEZER_ACCESS_TOKEN.name(), TEST_USER_ID).stream()
                .findFirst()
                .map(SessionData::getValue)
                .orElse(null);
    }


    public void saveDeezerAccessToken(String currentAccessToken) {
        SessionData sessionData = new SessionData(SessionTypeCode.DEEZER_ACCESS_TOKEN.name(), currentAccessToken, TEST_USER_ID);
        sessionDataRepository.save(sessionData);
    }
}

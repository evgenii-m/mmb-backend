package ru.pushkin.mma.data;

import org.springframework.stereotype.Component;

@Component
public class SettingsStorage {

    private String deezerAccessToken;


    public String getDeezerAccessToken() {
        return deezerAccessToken;
    }


    public void saveDeezerAccessToken(String currentAccessToken) {
        this.deezerAccessToken = currentAccessToken;
    }
}

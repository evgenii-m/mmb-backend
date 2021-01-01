package ru.pushkin.mmb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "service-config")
@Getter
@Setter
public class ServicePropertyConfig {
    private String deezerApplicationName;
    private String deezerApplicationApiId;
    private String deezerApplicationApiSecretKey;
    private Integer deezerApiServiceThreadPoolSize;
    private String deezerApplicationMonthlyPlaylistTitleFormat;
    private LastFm lastFm = new LastFm();

    @Component
    @EnableConfigurationProperties
    @ConfigurationProperties(prefix = "last-fm")
    @Getter
    @Setter
    public static class LastFm {
        private String applicationName;
        private String applicationApiKey;
        private String applicationApiSharedSecret;
        private String redirectUrl;
        private Integer retryTimeoutSec;
        private Integer sessionRetryCount;
    }
}

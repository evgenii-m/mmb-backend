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
public class ServiceConfig {
    private String deezerApplicationName;
    private String deezerApplicationApiId;
    private String deezerApplicationApiSecretKey;
    private Integer deezerApiServiceThreadPoolSize;
    private String deezerApplicationMonthlyPlaylistTitleFormat;

}

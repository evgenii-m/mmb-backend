package ru.pushkin.mmb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Validated
@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mmb.jwt")
@Getter
@Setter
public class JwtPropertyConfig {

    @Valid
    private final Encryption encryption = new Encryption();
    @NotNull
    private String privateKey;
    @NotNull
    private String publicKey;
    @NotNull
    private Long expirationTime;

    @Getter
    @Setter
    public static class Encryption {
        @NotNull
        private boolean enabled;
        @NotNull
        private String algorithm;
        @NotNull
        private String jwk;
        @NotNull
        private String jwkPassword;
        @NotNull
        private String jwkSalt;

    }

}

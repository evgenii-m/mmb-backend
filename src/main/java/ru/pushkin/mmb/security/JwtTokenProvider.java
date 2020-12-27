package ru.pushkin.mmb.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import ru.pushkin.mmb.config.JwtPropertyConfig;
import ru.pushkin.mmb.data.enumeration.SecurityRoleCode;
import ru.pushkin.mmb.security.token.TokenService;
import ru.pushkin.mmb.security.token.context.TokenContext;
import ru.pushkin.mmb.security.token.context.UserTokenContext;
import ru.pushkin.mmb.utils.TimeProvider;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final TimeProvider timeProvider;
    private final TokenService tokenService;
    private final JwtPropertyConfig jwtPropertyConfig;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private JsonWebKey jwk;


    @PostConstruct
    public void setup() throws JoseException {
        privateKey = KeyUtil.getPrivateKey(jwtPropertyConfig.getPrivateKey());
        publicKey = KeyUtil.getPublicKey(jwtPropertyConfig.getPublicKey());
        jwk = JsonWebKey.Factory.newJwk(jwtPropertyConfig.getEncryption().getJwk());
    }

    public String generateUserToken(String userId) {
        Instant now = timeProvider.nowDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        UserTokenContext tokenContext = new UserTokenContext(
                UUID.randomUUID().toString(),
                SecurityRoleCode.USER.getCode(),
                "",
                userId
        );
        return tokenService.createEncryptedToken(tokenContext, now, jwtPropertyConfig.getExpirationTime(), privateKey);
    }

    public Authentication getAuthentication(String token) {
        if (token == null) {
            return new UsernamePasswordAuthenticationToken(
                    null, null,
                    Collections.singletonList(new SimpleGrantedAuthority(SecurityRoleCode.UNAUTHORIZED.getCode()))
            );
        }

        JwtPropertyConfig.Encryption jwtEncryptionConfig = jwtPropertyConfig.getEncryption();
        if (jwtEncryptionConfig.isEnabled()) {
            token = JwtUtil.decryptToken(token, jwk, jwtEncryptionConfig.getAlgorithm());
        }

        Claims claims = JwtUtil.getClaims(token, publicKey);
        Object userId = claims.get(JwtTokenConstants.CLAIMS_USER_ID);
        String role = claims.get(JwtTokenConstants.CLAIMS_ROLE, String.class);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userId, token,
                        Collections.singletonList(new SimpleGrantedAuthority(role))
                );
        TokenContext tokenContext = tokenService.createUserTokenContext(claims);
        usernamePasswordAuthenticationToken.setDetails(tokenContext);
        return usernamePasswordAuthenticationToken;
    }
}

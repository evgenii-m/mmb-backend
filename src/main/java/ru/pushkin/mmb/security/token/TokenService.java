package ru.pushkin.mmb.security.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Service;
import ru.pushkin.mmb.config.JwtPropertyConfig;
import ru.pushkin.mmb.security.JwtTokenConstants;
import ru.pushkin.mmb.security.JwtUtil;
import ru.pushkin.mmb.security.token.context.TokenContext;
import ru.pushkin.mmb.security.token.context.UserTokenContext;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

/**
 * Service for create JWT token
 */
@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtPropertyConfig jwtPropertyConfig;
    private JsonWebKey jwk;

    @PostConstruct
    public void setup() throws JoseException {
        jwk = JsonWebKey.Factory.newJwk(jwtPropertyConfig.getEncryption().getJwk());
    }

    public TokenContext createUserTokenContext(Claims claims) {
        return new UserTokenContext(
                claims.get(Claims.ID, String.class),
                claims.get(JwtTokenConstants.CLAIMS_ROLE, String.class),
                claims.get(JwtTokenConstants.CLAIMS_IP, String.class),
                claims.get(JwtTokenConstants.CLAIMS_USER_ID, String.class)
        );
    }


    public <T extends TokenContext> String createToken(
            T tokenContext, Instant issuedAt, long expirationTime, PrivateKey privateKey
    ) {
        Claims claims = tokenContext.createClaims(JwtTokenConstants.DEFAULT_TOKEN_TYPE);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(issuedAt.plusSeconds(expirationTime)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public <T extends TokenContext> String createEncryptedToken(
            T tokenContext, Instant issuedAt, long expirationTime, PrivateKey privateKey
    ) {

        return JwtUtil.encryptToken(
                createToken(tokenContext, issuedAt, expirationTime, privateKey),
                jwk, jwtPropertyConfig.getEncryption().getAlgorithm()
        );
    }
}

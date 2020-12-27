package ru.pushkin.mmb.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.lang.JoseException;
import org.springframework.http.HttpHeaders;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.security.PublicKey;

@UtilityClass
public final class JwtUtil {

    /**
     * extract authentication token from request.
     *
     * @param req HttpServletRequest value with expected prefix {@link JwtTokenConstants#AUTHORIZATION_TOKEN_PREFIX}
     * @return String value
     */
    public static String resolveToken(ServletRequest req) {
        if (!(req instanceof HttpServletRequest)) {
            return null;
        }
        String bearerToken = ((HttpServletRequest) req).getHeader(HttpHeaders.AUTHORIZATION);
        return resolveToken(bearerToken);
    }

    public static String resolveToken(String bearerToken) {
        if (StringUtils.isEmpty(bearerToken)) {
            return null;
        }
        if (!StringUtils.startsWith(bearerToken, JwtTokenConstants.AUTHORIZATION_TOKEN_PREFIX)) {
            return null;
        }
        return bearerToken.substring(JwtTokenConstants.AUTHORIZATION_TOKEN_PREFIX.length());
    }

    /**
     * get payload information from token.
     *
     * @param token  token value
     * @param pubKey public key
     * @return Claims value
     */
    public static Claims getClaims(String token, PublicKey pubKey) {
        return Jwts.parser()
                .setSigningKey(pubKey)
                .parseClaimsJws(token)
                .getBody();
    }

    public static String encryptToken(String rawToken, JsonWebKey jwk, String algIdentifier) {
        try {
            JsonWebEncryption senderJwe = new JsonWebEncryption();
            senderJwe.setPlaintext(rawToken);

            senderJwe.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.DIRECT);
            senderJwe.setEncryptionMethodHeaderParameter(algIdentifier);
            senderJwe.setKey(jwk.getKey());

            return senderJwe.getCompactSerialization();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String decryptToken(String encryptedToken, JsonWebKey jwk, String algIdentifier) {
        try {
            JsonWebEncryption receiverJwe = new JsonWebEncryption();

            AlgorithmConstraints algConstraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                    KeyManagementAlgorithmIdentifiers.DIRECT);
            receiverJwe.setAlgorithmConstraints(algConstraints);
            AlgorithmConstraints encConstraints = new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                    algIdentifier);
            receiverJwe.setContentEncryptionAlgorithmConstraints(encConstraints);

            receiverJwe.setCompactSerialization(encryptedToken);
            receiverJwe.setKey(jwk.getKey());

            return receiverJwe.getPlaintextString();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }
}


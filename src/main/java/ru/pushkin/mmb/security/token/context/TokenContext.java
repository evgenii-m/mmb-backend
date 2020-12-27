package ru.pushkin.mmb.security.token.context;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import ru.pushkin.mmb.security.JwtTokenConstants;

@Data
public class TokenContext {
    private final String tokenId;
    private final String role;
    private final String clientIp;

    public Claims createClaims(String type) {
        Claims claims = Jwts.claims().setSubject(type);
        claims.put(Claims.ID, tokenId);
        claims.put(JwtTokenConstants.CLAIMS_ROLE, role);
        claims.put(JwtTokenConstants.CLAIMS_IP, clientIp);
        return claims;
    }
}

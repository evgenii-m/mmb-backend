package ru.pushkin.mmb.security.token.context;

import io.jsonwebtoken.Claims;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.pushkin.mmb.security.JwtTokenConstants;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class UserTokenContext extends TokenContext {
    private final String userId;

    public UserTokenContext(String userId, String tokenId, String role, String clientIp) {
        super(tokenId, role, clientIp);
        this.userId = userId;
    }

    @Override
    public Claims createClaims(String type) {
        Claims claims = super.createClaims(type);
        claims.put(JwtTokenConstants.CLAIMS_USER_ID, userId);
        return claims;
    }
}

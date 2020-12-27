package ru.pushkin.mmb.security;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.pushkin.mmb.security.token.context.UserTokenContext;


@UtilityClass
public class SecurityHelper {

    public String getUserIdFromToken() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserTokenContext) {
            UserTokenContext tokenContext =
                    (UserTokenContext) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return tokenContext.getUserId();
        } else {
            throw new CustomSecurityException("UserToken не задан");
        }
    }
}

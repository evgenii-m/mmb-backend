package ru.pushkin.mmb.security;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.pushkin.mmb.security.token.context.UserTokenContext;


@UtilityClass
public class SecurityHelper {

    public String getUserIdFromToken() {
        if (SecurityContextHolder.getContext().getAuthentication().getDetails() instanceof UserTokenContext) {
            UserTokenContext tokenContext =
                    (UserTokenContext) SecurityContextHolder.getContext().getAuthentication().getDetails();
            return tokenContext.getUserId();
        } else {
            throw new CustomSecurityException("UserToken не задан");
        }
    }
}

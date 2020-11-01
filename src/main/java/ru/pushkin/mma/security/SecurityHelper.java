package ru.pushkin.mma.security;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityHelper {

    public String getUserIdFromToken() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userDetails.getUserId();
        } else {
            return null;
        }
    }
}

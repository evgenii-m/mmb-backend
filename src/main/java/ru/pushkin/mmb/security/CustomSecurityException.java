package ru.pushkin.mmb.security;

import org.springframework.security.access.AccessDeniedException;

public class CustomSecurityException extends AccessDeniedException {

    public CustomSecurityException(String msg) {
        super(msg);
    }
}

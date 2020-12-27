package ru.pushkin.mmb.security;

public interface JwtTokenConstants {
    String CLAIMS_USER_ID = "uid";
    String CLAIMS_USER_SESSION_ID = "usid";
    String CLAIMS_RELATED_IDS = "relatedIds";
    String CLAIMS_ROLE = "role";
    String CLAIMS_ADMIN = "admin";
    String CLAIMS_MIS_USER_ID = "misUserId";
    String CLAIMS_IP = "ip";

    String AUTHORIZATION_HEADER = "Authorization";
    String AUTHORIZATION_TOKEN_PREFIX = "Bearer ";
    String DEFAULT_TOKEN_TYPE = "ACCESS";
}

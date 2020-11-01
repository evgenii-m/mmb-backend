package ru.pushkin.mma.data.enumeration;

public enum UserRoleCode {
    ROLE_USER("ROLE_USER");

    private String code;

    UserRoleCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

package ru.pushkin.mmb.data.enumeration;

public enum SecurityRoleCode {
    UNAUTHORIZED("ROLE_UNAUTHORIZED"),
    USER("ROLE_USER");

    private String code;

    SecurityRoleCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

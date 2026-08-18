package com.vnsearch.auth;

public enum Role { 
    USER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }

    @com.fasterxml.jackson.annotation.JsonCreator
    public static Role parse(String raw) {
        if (raw == null) {
            return USER;
        }
        try {
            return valueOf(raw.trim().toUpperCase(java.util.Local.ROOT));

        } catch (IllegalArgumentException e) {
            return USER;
        }
    }
}

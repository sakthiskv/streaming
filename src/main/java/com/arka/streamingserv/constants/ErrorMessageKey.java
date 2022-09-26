package com.arka.streamingserv.constants;

public enum ErrorMessageKey {


    AUTH_TOKEN_EXPIRED("auth.token.expired"),

    INVALID_TOKEN("invalid.token"),

    INVALID_SECURITY_CONTEXT("invalid.security.context");


    private final String value;

    ErrorMessageKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}


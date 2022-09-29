package com.arka.streamingserv.constants;

public enum ErrorMessageKey {


    AUTH_TOKEN_EXPIRED("auth.token.expired"),

    INVALID_TOKEN("invalid.token"),

    INVALID_SECURITY_CONTEXT("invalid.security.context"),

    EXCEPTION_OCCURRED("exception.occurred"),

    UNABLE_TO_GET_QUOTE_FROM_VENDOR("unable.to.get.quote.from.vendor");


    private final String value;

    ErrorMessageKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}


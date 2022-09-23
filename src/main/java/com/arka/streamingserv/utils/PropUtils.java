package com.arka.streamingserv.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PropUtils {

    @Autowired
    private Environment env;

    public String get(String key) {
        return env.getProperty(key);
    }

    public Integer getInteger(String key) {
        return Integer.parseInt(Optional.ofNullable(env.getProperty(key)).orElse("0"));
    }

    public Long getLong(String key) {
        return Long.parseLong(Optional.ofNullable(env.getProperty(key)).orElse("0"));
    }

}

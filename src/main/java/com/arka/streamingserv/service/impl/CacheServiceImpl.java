package com.arka.streamingserv.service.impl;

import com.arka.streamingserv.service.CacheService;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CacheServiceImpl implements CacheService {

    private final com.github.benmanes.caffeine.cache.Cache<String, JsonNode>
            CACHE = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(45))
            .maximumSize(1_000)
            .build();

    @Override
    public void putValueInCache(String key, JsonNode value){

        CACHE.put(key,value);

    }

    @Override
    public JsonNode getValueInCache(String key){

        return CACHE.getIfPresent(key);

    }

    @Override
    public void clearValueInCache(String key){

        CACHE.invalidate(key);

    }

    @Override
    public void clearAllValuesInCache(){

        CACHE.invalidateAll();

    }

}

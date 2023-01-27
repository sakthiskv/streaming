package com.arka.streamingserv.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface CacheService {

    void putValueInCache(String key, JsonNode value);

    JsonNode getValueInCache(String key);

    void clearValueInCache(String key);

    void clearAllValuesInCache();

}

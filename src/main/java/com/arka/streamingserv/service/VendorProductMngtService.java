package com.arka.streamingserv.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface VendorProductMngtService {

    Mono<JsonNode> getProducts(@SpringQueryMap Map<String, List<String>> queryMap, @RequestHeader Map<String, List<String>> headerMap);
}

package com.arka.streamingserv.service.webclient;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@ReactiveFeignClient(value = "procurement-service", url = "${com.arka.procurement.service.url}")
public interface ProcurementSevrvFeignClient {

    @PostMapping("/v1/procurement/fetch-plan")
    Mono<JsonNode> fetchPlan(@RequestHeader Map<String, List<String>> headerMap, @RequestBody JsonNode bodyJson);


}

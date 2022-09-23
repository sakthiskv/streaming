package com.arka.streamingserv.service.webclient;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@ReactiveFeignClient(value = "vendor-product-management-service", url = "${com.arka.vpm.service.url}")
public interface VendorProductMgmtFeignClient {

    @GetMapping("/v1/catalog/products")
    Mono<JsonNode> getProducts(@SpringQueryMap Map<String, List<String>> queryMap, @RequestHeader Map<String, List<String>> headerMap);

}

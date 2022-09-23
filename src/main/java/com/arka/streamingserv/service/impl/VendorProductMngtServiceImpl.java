package com.arka.streamingserv.service.impl;

import com.arka.helperlib.Exception.ServiceException;
import com.arka.streamingserv.service.CacheService;
import com.arka.streamingserv.service.VendorProductMngtService;
import com.arka.streamingserv.service.webclient.VendorProductMgmtFeignClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VendorProductMngtServiceImpl implements VendorProductMngtService {

    private static String PRODUCT = "PRODUCT";

    private static String DOT = ".";

    @Autowired
    VendorProductMgmtFeignClient vendorProductMgmtFeignClient;

    @Autowired
    CacheService cacheService;

    @Override
    public Mono<JsonNode> getProducts(Map<String, List<String>> queryMap, Map<String, List<String>> headerMap) {
        String queryMapString = PRODUCT + DOT + queryMap.toString();
        Optional<JsonNode> responseJson = Optional.ofNullable(cacheService.getValueInCache(queryMapString));
        System.out.println("cache product values ::"+ responseJson);
        return responseJson
                .map(Mono::just)
                .orElseGet(() -> {
                    System.out.println("Initiating service call to get products");
                    return vendorProductMgmtFeignClient.getProducts(queryMap, headerMap)
                            .doOnNext(resultJson -> {
                                System.out.println("Products ::\t"+ resultJson.size());
                                cacheService.putValueInCache(queryMapString, resultJson);
                            });
                });
    }
}

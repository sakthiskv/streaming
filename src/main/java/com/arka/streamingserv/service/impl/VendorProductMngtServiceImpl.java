package com.arka.streamingserv.service.impl;

import com.arka.helperlib.Exception.ServiceException;
import com.arka.streamingserv.service.CacheService;
import com.arka.streamingserv.service.VendorProductMngtService;
import com.arka.streamingserv.service.webclient.VendorProductMgmtFeignClient;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class VendorProductMngtServiceImpl implements VendorProductMngtService {

    private static String CACHE_KEY = "QUOTE.STREAM.PRODUCTS";
    private static String CATEGORY = "CATEGORY";
    private static String PRODUCT = "PRODUCT";
    private static String PRODUCT_FEATURES = "PRODUCT_FEATURES";
    private static String ADDONS = "ADDONS";
    private static String DOT = ".";

    @Autowired
    VendorProductMgmtFeignClient vendorProductMgmtFeignClient;

    @Autowired
    CacheService cacheService;

    @Override
    public Mono<JsonNode> getProducts(Map<String, List<String>> queryMap, Map<String, List<String>> headerMap) {
        Optional<JsonNode> responseJson = Optional.ofNullable(cacheService.getValueInCache(CACHE_KEY));
        System.out.println("cache products is Available ::" + responseJson.isPresent());
        System.out.println(headerMap);
        return responseJson
                .map(Mono::just)
                .orElseGet(() -> {
                    System.out.println("Initiating service call to get products");
                    return vendorProductMgmtFeignClient.getProducts(queryMap, headerMap)
                            .doOnNext(resultJson -> {
                                System.out.println("Products ::\t" + resultJson.size());
                                cacheService.putValueInCache(CACHE_KEY, resultJson);
                            });
                }).doOnError(e -> {
                    System.out.println("Exception in get products :" + e.getMessage());
                });
    }

    @Override
    public Mono<JsonNode> getAddons(Map<String, List<String>> queryMap, Map<String, List<String>> headerMap) {
        String addonsQueryMap = ADDONS + DOT + queryMap.toString();
        Optional<JsonNode> responseJson = Optional.ofNullable(cacheService.getValueInCache(addonsQueryMap));
        System.out.println("Addons isAvailable in cache ::" + responseJson.isPresent());
        return responseJson
                .map(Mono::just)
                .orElseGet(() -> {
                            System.out.println("\n\nmaking api call to get addons\n\n");
                            return vendorProductMgmtFeignClient.getAddons(queryMap, headerMap)
                                    .doOnNext(resultJson -> cacheService.putValueInCache(addonsQueryMap, resultJson));
                        }
                ).doOnError(e -> {
                    System.out.println("\n\nGet addons failed with following exception::" + e.getMessage());
                });
    }

    @Override
    public Mono<JsonNode> getCategories(@SpringQueryMap Map<String, List<String>> queryMap, @RequestHeader Map<String, List<String>> headerMap) {

        String queryMapString = CATEGORY + DOT + queryMap.toString();
        Optional<JsonNode> responseJson = Optional.ofNullable(cacheService.getValueInCache(queryMapString));
        return responseJson
                .map(Mono::just)
                .orElseGet(() ->
                        vendorProductMgmtFeignClient.getCategories(queryMap, headerMap)
                                .doOnNext(resultJson -> cacheService.putValueInCache(queryMapString, resultJson))
                ).doOnError(e -> {
                    System.out.println("\n\nGet categories failed with following exception::" + e.getMessage());
                });
    }

    @Override
    public Mono<JsonNode> getCategoryAttributesById(@PathVariable String categoryId, @SpringQueryMap Map<String, List<String>> queryMap, @RequestHeader Map<String, List<String>> headerMap) {

        String idAndQueryMapString = categoryId + queryMap.toString();
        Optional<JsonNode> responseJson = Optional.ofNullable(cacheService.getValueInCache(idAndQueryMapString));
        System.out.println("CategoryAttributeByID is available in cache: "+ responseJson.isPresent());
        return responseJson
                .map(Mono::just)
                .orElseGet(() ->
                        vendorProductMgmtFeignClient.getCategoryAttributesById(categoryId, queryMap, headerMap)
                                .doOnNext(resultJson -> cacheService.putValueInCache(idAndQueryMapString, resultJson))

                ).doOnError(e -> {
                    System.out.println("\n\nGet category by id failed with following exception::" + e.getMessage());
                });
    }

    @Override
    public Mono<JsonNode> getProductFeaturesV2(Map<String, List<String>> queryMap, Map<String, List<String>> headerMap) {
        String queryMapString = PRODUCT_FEATURES + DOT + queryMap.toString() + headerMap.toString();
        Optional<JsonNode> responseJson = Optional.ofNullable(cacheService.getValueInCache(queryMapString));
        return responseJson
                .map(Mono::just)
                .orElseGet(() ->
                        vendorProductMgmtFeignClient.getProductFeaturesV2(queryMap, headerMap)
                                .doOnNext(resultJson -> cacheService.putValueInCache(queryMapString, resultJson))
                ).doOnError(e -> {
                    System.out.println("\n\nGet product features failed with following exception::" + e.getMessage());
                });

    }
}

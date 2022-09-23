package com.arka.streamingserv.service.impl;

import com.arka.helperlib.utils.CriteriaUtils;
import com.arka.helperlib.utils.JsonUtils;
import com.arka.streamingserv.service.QuoteService;
import com.arka.streamingserv.service.VendorProductMngtService;
import com.arka.streamingserv.vo.quote.FetchPlanReqVO;
import com.arka.streamingserv.vo.quote.FetchQuoteReqVO;
import com.arka.streamingserv.vo.quote.FetchQuoteVO;
import com.arka.streamingserv.vo.quote.QuoteVO;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

@Service
public class QuoteServiceImpl implements QuoteService {

    @Autowired
    VendorProductMngtService vendorProductMngtService;

    List<String> productCodes = new ArrayList<>(Arrays.asList("HDFCERGO_HEALTH_1", "HDFCERGO_HEALTH_2", "HDFCERGO_HEALTH_3", "HDFCERGO_HEALTH_4"));

    @Override
    public Flux<QuoteVO> fetchQuotes(Map<String, String> headers, Mono<FetchQuoteReqVO> fetchQuoteReqVOMono) {
        System.out.println(JsonUtils.toJson(fetchQuoteReqVOMono));
        System.out.println("came to service layer");
        Map<String, List<String>> queryMap = new HashMap<>();
        queryMap.put(CriteriaUtils.NO_OF_RECORDS, Collections.singletonList(CriteriaUtils.ALL_RECORDS_VALUE));
        List<FetchPlanReqVO> fetchPlanReqVOList = new ArrayList<>();
        Mono<JsonNode>  productJson = vendorProductMngtService.getProducts(queryMap, new HashMap<>());
        return Flux.fromStream(Stream.generate(() -> fetchQuoteReqVOMono.flatMap(fetchQuoteReqVO ->
                productJson.flatMap(productRes -> {
            productRes.get("items").spliterator().forEachRemaining(product -> {
                System.out.println("arjaaaaa\t\t"+productCodes.contains(product.get("code").asText()));
                if (productCodes.contains(product.get("code").asText())) {
                    System.out.println("arjaaaaa\t\t"+product.get("_id").asText());
                    FetchPlanReqVO fetchPlanReqVO = new FetchPlanReqVO();
                    fetchPlanReqVO.setEnquiryId(fetchQuoteReqVO.getEnquiryId());
                    fetchPlanReqVO.setTenure(fetchQuoteReqVO.getTenure());
                    fetchPlanReqVO.setProductId(product.get("_id").asText());
                    fetchPlanReqVOList.add(fetchPlanReqVO);
                }
            });
            System.out.println("before \t\t"+JsonUtils.toJson(fetchPlanReqVOList));
            System.out.println("final \t\t"+JsonUtils.toJson(fetchPlanReqVOList));
                    return null;
                }))));
    }

    private Flux<FetchPlanReqVO> formRequest(Mono<FetchQuoteReqVO> fetchQuoteReqVOMono) {
        Map<String, List<String>> queryMap = new HashMap<>();
        queryMap.put(CriteriaUtils.NO_OF_RECORDS, Collections.singletonList(CriteriaUtils.ALL_RECORDS_VALUE));
        List<FetchPlanReqVO> fetchPlanReqVOList = new ArrayList<>();
        Mono<JsonNode>  productJson = vendorProductMngtService.getProducts(queryMap, new HashMap<>());
        productJson.flatMap(productRes -> {
            productRes.get("items").forEach(product -> {
                System.out.println("arjaaaaa\t\t" + productCodes.contains(product.get("code").asText()));
                if (productCodes.contains(product.get("code").asText())) {
                    System.out.println("arjaaaaa\t\t" + product.get("_id").asText());
                    FetchPlanReqVO fetchPlanReqVO = new FetchPlanReqVO();
                    fetchPlanReqVO.setEnquiryId(fetchQuoteReqVO.getEnquiryId());
                    fetchPlanReqVO.setTenure(fetchQuoteReqVO.getTenure());
                    fetchPlanReqVO.setProductId(product.get("_id").asText());
                    fetchPlanReqVOList.add(fetchPlanReqVO);
                }});
            return null;
        });
        return fetchQuoteReqVOMono.flatMapIterable(fetchQuoteReqVO ->
              });
            return fetchPlanReqVOList;
        }));
}

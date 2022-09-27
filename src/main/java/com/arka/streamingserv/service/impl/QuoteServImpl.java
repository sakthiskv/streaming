package com.arka.streamingserv.service.impl;

import com.arka.helperlib.constants.enums.CurrencyCode;
import com.arka.helperlib.utils.CriteriaUtils;
import com.arka.helperlib.utils.JsonUtils;
import com.arka.streamingserv.service.QuoteService;
import com.arka.streamingserv.service.VendorProductMngtService;
import com.arka.streamingserv.service.webclient.ProcurementSevrvFeignClient;
import com.arka.streamingserv.vo.quote.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
public class QuoteServImpl implements QuoteService {

    private static double VENDOR_TAX_PERCENTAGE = 18;

    @Autowired
    VendorProductMngtService vendorProductMngtService;

    @Autowired
    ProcurementSevrvFeignClient procurementSevrvFeignClient;

    List<String> productCodes = new ArrayList<>(Arrays.asList("HDFCERGO_HEALTH_1", "HDFCERGO_HEALTH_2", "HDFCERGO_HEALTH_3", "HDFCERGO_HEALTH_4"));

    @Override
    public Flux<FetchQuoteResponseVO> fetchQuotes(Map<String, String> headers, Mono<QuoteReqVO> fetchQuoteReqVOMono) {
        System.out.println(JsonUtils.toJson(fetchQuoteReqVOMono));
        System.out.println("came to service layer");
        Flux<FetchPlanRequestVO> fetchPlanReqVOFlux = formRequest(fetchQuoteReqVOMono);
        System.out.println("before \t\t" + JsonUtils.toJson(fetchPlanReqVOFlux));

        return fetchPlanReqVOFlux.delayElements(Duration.ofSeconds(2)).flatMap(fetchPlanRequestVO -> {
            System.out.println("\n\nrequest \t\t"+ JsonUtils.toJson(fetchPlanRequestVO));
            return procurementSevrvFeignClient.fetchPlan(new HashMap<>(), JsonUtils.toJson(fetchPlanRequestVO))
                    .flatMapMany(res -> {
                        System.out.println("\n\\n\nbefore");
                        System.out.println(res);
                        FetchQuoteResponseVO fetchQuoteResponseVO = populateResponse(res);
                        System.out.println("quote response :::"+ JsonUtils.toJson(fetchQuoteResponseVO));
                        return Mono.just(fetchQuoteResponseVO);
                    });
        });
    }

    private Flux<FetchPlanRequestVO> formRequest(Mono<QuoteReqVO> fetchQuoteReqVOMono) {
        System.out.println(productCodes);
        Map<String, List<String>> queryMap = new HashMap<>();
        queryMap.put(CriteriaUtils.NO_OF_RECORDS, Collections.singletonList(CriteriaUtils.ALL_RECORDS_VALUE));
        List<FetchPlanRequestVO> fetchPlanRequestVOList = new ArrayList<>();
        return fetchQuoteReqVOMono.flux().flatMap(quoteReqVO -> {
            return vendorProductMngtService.getProducts(queryMap, new HashMap<>()).flatMapIterable(productRes -> {
                productRes.get("items").forEach(product -> {
                    if (productCodes.contains(product.get("code").asText())) {
                        System.out.println("arjaaaaa\t\t" + product.get("_id").asText());
                        FetchPlanRequestVO fetchPlanRequestVO = new FetchPlanRequestVO();
                        fetchPlanRequestVO.setEnquiryId(quoteReqVO.getEnquiryId());
                        fetchPlanRequestVO.setTenure(quoteReqVO.getTenure());
                        fetchPlanRequestVO.setProductId(product.get("_id").asText());
                        fetchPlanRequestVOList.add(fetchPlanRequestVO);
                    }
                });
                return fetchPlanRequestVOList;
            });
        });
    }

    private FetchQuoteResponseVO populateResponse(JsonNode responseJson) {
        FetchQuoteResponseVO fetchQuoteResponseVO = new FetchQuoteResponseVO();
        if(JsonUtils.isValidField(responseJson, "output_quote_params")
                && JsonUtils.isValidField(responseJson.get("output_quote_params"), JsonUtils.JSON_ARRAY_ITEMS_KEY)
                && JsonUtils.isValidIndex(responseJson.get("output_quote_params").get(JsonUtils.JSON_ARRAY_ITEMS_KEY), 0)
                && JsonUtils.isValidField(responseJson.get("output_quote_params").get(JsonUtils.JSON_ARRAY_ITEMS_KEY).get(0),"attributes")) {
            List<QuoteResVO> quotes = new ArrayList<>();
            responseJson.get("output_quote_params").get(JsonUtils.JSON_ARRAY_ITEMS_KEY).forEach(outputQuoteParamJson->{
                JsonNode attributesJson = outputQuoteParamJson.get("attributes");
                QuoteResVO quoteResVO = new QuoteResVO();
                MoneyVO sumInsuredMoneyVO = new MoneyVO(attributesJson.get("AH-PRODUCT_SUM_INSURED").get("value").asDouble(), CurrencyCode.INR);
                quoteResVO.setSumInsured(sumInsuredMoneyVO);
                quoteResVO.setQuoteKey(outputQuoteParamJson.get("quoteKey").asText());
                InsurerVO insurerVO = new InsurerVO(outputQuoteParamJson.get("vendorName").asText(), responseJson.get("image_url").asText());
                quoteResVO.setInsurer(insurerVO);
                quoteResVO.setProductCode(responseJson.get("productCode").asText());
//                quoteResVO.setTenure(attributesJson.get("AI-POLICY_TERM").get("value").asInt());
                MoneyVO basePremium = new MoneyVO(attributesJson.get("AH-BASE_PREMIUM").get("value").asDouble(), CurrencyCode.INR);
                MoneyVO totalPremium = new MoneyVO(attributesJson.get("AH-PREMIUM_AMOUNT").get("value").asDouble(), CurrencyCode.INR);
                MoneyVO premiumWithoutDiscount = new MoneyVO(attributesJson.get("AH-PREMIUM_WITHOUT_DISCOUNT").get("value").asDouble(), CurrencyCode.INR);
                PremiumVO premiumVO = new PremiumVO(basePremium, premiumWithoutDiscount, new HashMap<>(), totalPremium, VENDOR_TAX_PERCENTAGE);
                quoteResVO.setPremiumWithoutTax(premiumVO);
                quotes.add(quoteResVO);
            });
            fetchQuoteResponseVO.setEnquiryId(responseJson.get("enquiryId").asText());
            fetchQuoteResponseVO.setItems(quotes);
            return fetchQuoteResponseVO;
        }
        return fetchQuoteResponseVO;
    }
}

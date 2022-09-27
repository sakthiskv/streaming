package com.arka.streamingserv.service.impl;

import com.arka.helperlib.Exception.ServiceException;
import com.arka.helperlib.constants.enums.CurrencyCode;
import com.arka.helperlib.utils.CriteriaUtils;
import com.arka.helperlib.utils.JsonUtils;
import com.arka.streamingserv.dto.AttributeDTO;
import com.arka.streamingserv.dto.CategoryAttributeDTO;
import com.arka.streamingserv.service.QuoteService;
import com.arka.streamingserv.service.VendorProductMngtService;
import com.arka.streamingserv.service.webclient.ProcurementSevrvFeignClient;
import com.arka.streamingserv.utils.CategoryConfig;
import com.arka.streamingserv.vo.quote.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class QuoteServiceImpl  {

    public static final String ITEMS = "items";
    public static final String CODE = "code";
    public static final String ID = "_id";
    public static final String OUTPUT_QUOTE_PARAMS = "output_quote_params";
    public static final String CATEGORY_ID = "category_id";
    private static double VENDOR_TAX_PERCENTAGE = 18;
    private static String PREMIUM_FIELD = "-premium";

    private static String EMPTY = "";

    @Autowired
    VendorProductMngtService vendorProductMngtService;

    @Autowired
    ProcurementSevrvFeignClient procurementSevrvFeignClient;

    @Autowired
    CategoryConfig categoryConfig;

//    @Override
    public Flux<FetchQuoteResponseVO> fetchQuotes(Map<String, String> headers, Mono<QuoteReqVO> fetchQuoteReqVOMono) {
        System.out.println("\n\n\n Headers \t" + headers);
        Map<String, List<String>> headerMap = new HashMap<>();
        headers.entrySet().stream().forEach(header -> headerMap.put(header.getKey(), Collections.singletonList(header.getValue())));
        Flux<FetchPlanRequestVO> fetchPlanRequestVOFlux = formRequest(headerMap, fetchQuoteReqVOMono);
        FetchQuoteResponseVO fetchQuoteResponseVO = new FetchQuoteResponseVO();
        try {
            return fetchPlanRequestVOFlux.delayElements(Duration.ofSeconds(2)).flatMap(fetchPlanRequestVO -> {
                QuoteRequestVO quoteRequestVO = new QuoteRequestVO();
                quoteRequestVO.setCategoryId(fetchPlanRequestVO.getCategoryId());
                quoteRequestVO.setProductCode(fetchPlanRequestVO.getProductCode());
                quoteRequestVO.setEnquiryId(fetchPlanRequestVO.getEnquiryId());
                quoteRequestVO.setTenure(fetchPlanRequestVO.getTenure());
                quoteRequestVO.setProductId(fetchPlanRequestVO.getProductId());
                fetchQuoteResponseVO.setErrorMsg(fetchPlanRequestVO.getProductCode() + ".Unable to get quote");
                System.out.println("\nrequest \t\t"+ JsonUtils.toJson(quoteRequestVO));
                return procurementSevrvFeignClient.fetchPlan(headerMap, JsonUtils.toJson(quoteRequestVO))
                        .flatMap(quoteResponse -> {
                            JsonNode addonJson = fetchPlanRequestVO.getAddonJson();
                            CategoryAttributeDTO categoryAttributeDTO = fetchPlanRequestVO.getCategoryAttributeDTO();
                            System.out.println("\nQuote api call response:: \t" + quoteResponse);
                            return Mono.just(fetchQuoteResponseVO);
//                            return populateResponse(quoteRequestVO, quoteResponse, addonJson, categoryAttributeDTO, headerMap).flatMap(fetchQuoteResponse -> {
//                                System.out.println("\n After response rewrite quote response ::" + JsonUtils.toJson(fetchQuoteResponse));
//                                return Mono.just(fetchQuoteResponse);
//                            });
                        }).doOnError(e -> {
                            System.out.println("Inside doOnError::\t" + e.getMessage());
                            fetchQuoteResponseVO.setEnquiryId(fetchPlanRequestVO.getEnquiryId());
                            Flux.just(fetchQuoteResponseVO);
                        });
            }).doOnError(e -> {
                System.out.println("Inside 2 doOnError::\t" + e.getMessage());
                fetchQuoteResponseVO.setErrorMsg("Given product code is not available.");
                Flux.just(fetchQuoteResponseVO);
            });
        } catch (Exception e) {
            System.out.println("\n\nException Occurred \t" + e.getMessage());
            throw new ServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Flux<FetchPlanRequestVO> formRequest(Map<String, List<String>> headers, Mono<QuoteReqVO> fetchQuoteReqVOMono) {
        Map<String, List<String>> queryMap = new HashMap<>();
        queryMap.put(CriteriaUtils.NO_OF_RECORDS, Collections.singletonList(CriteriaUtils.ALL_RECORDS_VALUE));
        return fetchQuoteReqVOMono.flatMapMany(quoteReqVO -> {
            List<FetchPlanRequestVO> fetchPlanRequestVOList = new ArrayList<>();
            AtomicReference<String> categoryId = new AtomicReference<>("");
            return vendorProductMngtService.getProducts(queryMap, headers).flatMapMany(productRes -> {
                productRes.get(ITEMS).forEach(product -> {
                    if (Objects.isNull(quoteReqVO.getProductCode())) {
                        System.out.println("product code and ID ::\t" + product.get(CODE) + "\t" + product.get(ID).asText());
                        fetchPlanRequestVOList.add(formFetchPlanRequest(quoteReqVO, product.get(ID).asText(), product.get(CATEGORY_ID).asText(), product.get(CODE).asText()));
                    } else if (Objects.nonNull(quoteReqVO.getProductCode()) && quoteReqVO.getProductCode().name().equalsIgnoreCase(product.get(CODE).asText())) {
                        fetchPlanRequestVOList.add(formFetchPlanRequest(quoteReqVO, product.get(ID).asText(), product.get(CATEGORY_ID).asText(), product.get(CODE).asText()));
                    }
                    categoryId.set(StringUtils.isEmpty(categoryId.get()) && JsonUtils.isValidField(product, CATEGORY_ID) ? product.get(CATEGORY_ID).asText() : categoryId.get());
                });
                System.out.println("\n\n list request :::" + fetchPlanRequestVOList);
                System.out.println("categoryId :::" + categoryId);
                Map<String, List<String>> queryParam = new HashMap<>();
                queryParam.put("category_id", Collections.singletonList(categoryId.get()));
                Mono<JsonNode> addonMono = vendorProductMngtService.getAddons(queryParam, headers);
                Mono<CategoryAttributeDTO> categoryAttributeDTOMono = categoryConfig.getCategoryAttributesDetails(categoryId.get());
                return Flux.fromIterable(fetchPlanRequestVOList).flatMap(fetchPlanRequestVO -> Mono.zip(addonMono, categoryAttributeDTOMono).flatMapIterable(objects -> {
                    JsonNode addonJson = objects.getT1();
                    CategoryAttributeDTO categoryAttributeDTO = objects.getT2();
                    fetchPlanRequestVO.setAddonJson(addonJson);
                    fetchPlanRequestVO.setCategoryAttributeDTO(categoryAttributeDTO);
                    return fetchPlanRequestVOList;
                }));
            }).doOnError(e -> {
                System.out.println("Getting Exception while doing get products call :: " + e.getMessage());
                Mono.error(new ServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
            });
        }).doOnError(e -> {
            System.out.println("\n\nInvalid request::: " + e.getMessage());
            Mono.error(new ServiceException(e.getMessage(), HttpStatus.BAD_REQUEST));
        });
    }

    private Mono<FetchQuoteResponseVO> populateResponse(QuoteRequestVO quoteRequestVO, JsonNode responseJson, JsonNode addonJson, CategoryAttributeDTO categoryAttributeDTO, Map<String, List<String>> headers) {
        FetchQuoteResponseVO fetchQuoteResponseVO = new FetchQuoteResponseVO();
        fetchQuoteResponseVO.setProductCode(quoteRequestVO.getProductCode());
        try {
            if (JsonUtils.isValidField(responseJson, OUTPUT_QUOTE_PARAMS)
                    && JsonUtils.isValidField(responseJson.get(OUTPUT_QUOTE_PARAMS), JsonUtils.JSON_ARRAY_ITEMS_KEY)
                    && JsonUtils.isValidIndex(responseJson.get(OUTPUT_QUOTE_PARAMS).get(JsonUtils.JSON_ARRAY_ITEMS_KEY), 0)
                    && JsonUtils.isValidField(responseJson.get(OUTPUT_QUOTE_PARAMS).get(JsonUtils.JSON_ARRAY_ITEMS_KEY).get(0), "attributes")) {
                List<QuoteResVO> quotes = new ArrayList<>();
                System.out.println("\n\naddonJson :::" + addonJson);
                if (isValidCategoryResp(categoryAttributeDTO, responseJson) && isValidAddonJson(addonJson, responseJson)) {
                    List<String> addonPremiumAttributes = getAddonPremiumAttributes(addonJson);
                    responseJson.get("output_quote_params").get(JsonUtils.JSON_ARRAY_ITEMS_KEY).forEach(outputQuoteParamJson -> {
                        JsonNode attributesJson = outputQuoteParamJson.get("attributes");
                        QuoteResVO quoteVO = new QuoteResVO();
                        MoneyVO sumInsuredMoneyVO = new MoneyVO(attributesJson.get("AH-PRODUCT_SUM_INSURED").get("value").asDouble(), CurrencyCode.INR);
                        quoteVO.setSumInsured(sumInsuredMoneyVO);
                        quoteVO.setQuoteKey(outputQuoteParamJson.get("quoteKey").asText());
                        InsurerVO insurerVO = new InsurerVO(outputQuoteParamJson.get("vendorName").asText(), responseJson.get("image_url").asText());
                        quoteVO.setInsurer(insurerVO);
                        quoteVO.setProductCode(responseJson.get("productCode").asText());
                        MoneyVO basePremium = new MoneyVO(attributesJson.get("AH-BASE_PREMIUM").get("value").asDouble(), CurrencyCode.INR);
                        MoneyVO totalPremium = new MoneyVO(attributesJson.get("AH-PREMIUM_AMOUNT").get("value").asDouble(), CurrencyCode.INR);
                        MoneyVO premiumWithoutDiscount = null;
                        if (JsonUtils.isValidField(attributesJson, "AH-PREMIUM_WITHOUT_DISCOUNT")) {
                            premiumWithoutDiscount = new MoneyVO(attributesJson.get("AH-PREMIUM_WITHOUT_DISCOUNT").get("value").asDouble(), CurrencyCode.INR);
                        }
                        Map<String, MoneyVO> addOns = getAddonMap(attributesJson, addonPremiumAttributes, categoryAttributeDTO.getCategoryAttributeCodeMap());
                        PremiumVO premiumVO = new PremiumVO(basePremium, premiumWithoutDiscount, addOns, totalPremium, VENDOR_TAX_PERCENTAGE);
                        quoteVO.setPremiumWithoutTax(premiumVO);
                        if (quoteRequestVO.getTenure() == attributesJson.get("AI-POLICY_TERM").get("value").asInt())
                            quotes.add(quoteVO);

                    });
                    fetchQuoteResponseVO.setEnquiryId(responseJson.get("enquiryId").asText());
                    fetchQuoteResponseVO.setTenure(quoteRequestVO.getTenure());
                    fetchQuoteResponseVO.setItems(quotes);
                    return Mono.just(fetchQuoteResponseVO);
                }
                fetchQuoteResponseVO.setErrorMsg("Unable to get Quotes.");
                return Mono.just(fetchQuoteResponseVO);
            }
            fetchQuoteResponseVO.setErrorMsg("Unable to get Quotes.");
            return Mono.just(fetchQuoteResponseVO);
        } catch (Exception e) {
            fetchQuoteResponseVO.setErrorMsg("Exception occurred.");
            return Mono.just(fetchQuoteResponseVO);
        }
    }

    private FetchPlanRequestVO formFetchPlanRequest(QuoteReqVO quoteReqVO, String productId, String categoryId, String code) {
        FetchPlanRequestVO fetchPlanRequestVO = new FetchPlanRequestVO();
        fetchPlanRequestVO.setEnquiryId(quoteReqVO.getEnquiryId());
        fetchPlanRequestVO.setTenure(quoteReqVO.getTenure());
        fetchPlanRequestVO.setProductId(productId);
        fetchPlanRequestVO.setCategoryId(categoryId);
        fetchPlanRequestVO.setProductCode(code);
        return fetchPlanRequestVO;
    }

    private Map<String, MoneyVO> getAddonMap(JsonNode attributesJson, List<String> addonPremiumAttributes,
                                             Map<String, AttributeDTO> categoryAttributeCodeMap) {
        Map<String, MoneyVO> addOnsMap = new HashMap<>();
        addonPremiumAttributes.forEach(eachAttr -> {
            if (JsonUtils.isValidField(attributesJson, eachAttr) && categoryAttributeCodeMap.containsKey(eachAttr)) {
                addOnsMap.put(categoryAttributeCodeMap.get(eachAttr).getAlias().replace(PREMIUM_FIELD, EMPTY),
                        new MoneyVO(attributesJson.get(eachAttr).get("value").asDouble(), CurrencyCode.INR));
            }
        });
        return addOnsMap;
    }

    private boolean isValidAddonJson(JsonNode addonJson, JsonNode responseJson) {
        return !JsonUtils.isValidField(addonJson, JsonUtils.ERROR) && !getAddonPremiumAttributes(addonJson).isEmpty();
    }

    private boolean isValidCategoryResp(CategoryAttributeDTO categoryAttributeDTO, JsonNode responseJson) {
        return Objects.nonNull(categoryAttributeDTO.getCategoryAttributeMap());
    }

    private List<String> getAddonPremiumAttributes(JsonNode responseJson) {
        List<String> addonPremiumAttributes = new ArrayList<>();
        responseJson.fields().forEachRemaining((eachEntry) -> {
            JsonNode valueJson = eachEntry.getValue();
            if (JsonUtils.isValidField(valueJson, "codes") && JsonUtils.isValidField(valueJson.get("codes"), "out")) {
                addonPremiumAttributes.add(valueJson.get("codes").get("out").asText());
            }
        });
        return addonPremiumAttributes;
    }
}

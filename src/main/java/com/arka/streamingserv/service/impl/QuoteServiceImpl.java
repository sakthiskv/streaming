package com.arka.streamingserv.service.impl;

import com.arka.helperlib.Exception.ServiceException;
import com.arka.helperlib.constants.enums.CurrencyCode;
import com.arka.helperlib.constants.vo.ErrorVO;
import com.arka.helperlib.utils.CriteriaUtils;
import com.arka.helperlib.utils.JsonUtils;
import com.arka.streamingserv.constants.ErrorMessageKey;
import com.arka.streamingserv.dto.AttributeDTO;
import com.arka.streamingserv.dto.CategoryAttributeDTO;
import com.arka.streamingserv.service.QuoteService;
import com.arka.streamingserv.service.VendorProductMngtService;
import com.arka.streamingserv.service.webclient.ProcurementSevrvFeignClient;
import com.arka.streamingserv.utils.CategoryConfig;
import com.arka.streamingserv.utils.ErrorUtils;
import com.arka.streamingserv.vo.quote.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuoteServiceImpl implements QuoteService {

    public static final String ITEMS = "items";
    public static final String CODE = "code";
    public static final String ID = "_id";
    public static final String OUTPUT_QUOTE_PARAMS = "output_quote_params";
    private static double VENDOR_TAX_PERCENTAGE = 18;
    private static String PREMIUM_FIELD = "-premium";

    private static String EMPTY = "";

    @Autowired
    VendorProductMngtService vendorProductMngtService;

    @Autowired
    ProcurementSevrvFeignClient procurementSevrvFeignClient;

    @Autowired
    CategoryConfig categoryConfig;

    @Override
    public Flux<FetchQuoteResponseVO> fetchQuotes(Map<String, String> headers, Mono<QuoteReqVO> fetchQuoteReqVOMono) {
        System.out.println("\n\n\n Headers \t" + headers);
        Map<String, List<String>> headerMap = new HashMap<>();
        headers.entrySet().stream().forEach(header -> headerMap.put(header.getKey(), Collections.singletonList(header.getValue())));
        Mono<List<FetchPlanRequestVO>> fetchPlanReqVOListMono = formRequest(headerMap, fetchQuoteReqVOMono);
        FetchQuoteResponseVO fetchQuoteResponseVO = new FetchQuoteResponseVO();
//        Flux<FetchQuoteResponseVO> s = Flux.fromIterable(new ArrayList<>()).flatMap(fetchPlanRequestVO -> {
//            System.out.println("\nrequest \t\t" + JsonUtils.toJson(fetchPlanRequestVO));
//            return procurementSevrvFeignClient.fetchPlan(headerMap, JsonUtils.toJson(fetchPlanRequestVO))
//                    .flatMap(quoteResponse -> {
//                        System.out.println("\nQuote api call response:: \t" + quoteResponse);
//                        return populateResponse(quoteResponse, headerMap).flatMap(fetchQuoteResponse -> {
////                            System.out.println("\n After response rewrite quote response ::" + JsonUtils.toJson(fetchQuoteResponse));
//                            return Mono.just(fetchQuoteResponse);
//                        });
//                    }).doOnError(e -> {
//                        System.out.println("Inside doOnError::\t" + e.getMessage());
////                        fetchQuoteResponseVO.setEnquiryId(fetchPlanRequestVO.getEnquiryId());
////                        fetchQuoteResponseVO.setErrorMsg(fetchPlanRequestVO.getProductCode() + ".Unable to get quote");
//                        System.out.println("Inside 1 doOnError::\t" + e.getMessage());
//                        Flux.just(fetchQuoteResponseVO);
//                    });
//        });
        try {
            return fetchPlanReqVOListMono.flatMapMany(fetchPlanRequestVOList -> {
                if (fetchPlanRequestVOList.size() > 0) {
                    Map<String, List<String>> queryParam = new HashMap<>();
                    String categoryId = fetchPlanRequestVOList.get(0).getCategoryId();
                    queryParam.put("category_id", Collections.singletonList(categoryId));
                    Mono<JsonNode> addonMono = vendorProductMngtService.getAddons(queryParam, headerMap);
                    Mono<CategoryAttributeDTO> categoryAttributeDTOMono = categoryConfig.getCategoryAttributesDetails(categoryId);
                    return Flux.fromIterable(fetchPlanRequestVOList).flatMap(fetchPlanRequestVO -> {
                        fetchQuoteResponseVO.setErrorMsg(fetchPlanRequestVO.getProductCode() + ".Unable to get quote");
                        System.out.println("\nrequest \t\t" + JsonUtils.toJson(fetchPlanRequestVO));
                        Mono<JsonNode> quoteResponseMono = procurementSevrvFeignClient.fetchPlan(headerMap, JsonUtils.toJson(fetchPlanRequestVO));
                        return Mono.zip(quoteResponseMono, addonMono, categoryAttributeDTOMono)
                                .flatMap(objects -> {
                                    JsonNode quoteResponse = objects.getT1();
                                    JsonNode addonJson = objects.getT2();
                                    CategoryAttributeDTO categoryAttributeDTO = objects.getT3();
                                    System.out.println("\nQuote api call response:: \t" + quoteResponse);
                                    return populateResponse(fetchPlanRequestVO, quoteResponse, addonJson, categoryAttributeDTO, headerMap).flatMap(fetchQuoteResponse -> {
                                        System.out.println("\n After response rewrite quote response ::" + JsonUtils.toJson(fetchQuoteResponse));
                                        return Mono.just(fetchQuoteResponse);
                                    });
                                }).doOnError(e -> {
                                    System.out.println("Inside doOnError::\t" + e.getMessage());
                                    fetchQuoteResponseVO.setEnquiryId(fetchPlanRequestVO.getEnquiryId());
                                    Flux.just(fetchQuoteResponseVO);
                                });
                    });
                }
                fetchQuoteResponseVO.setErrorMsg("Given product code is not available.");
                return Flux.just(fetchQuoteResponseVO);
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

    private Mono<List<FetchPlanRequestVO>> formRequest(Map<String, List<String>> headers, Mono<QuoteReqVO> fetchQuoteReqVOMono) {
        Map<String, List<String>> queryMap = new HashMap<>();
        queryMap.put(CriteriaUtils.NO_OF_RECORDS, Collections.singletonList(CriteriaUtils.ALL_RECORDS_VALUE));
        return fetchQuoteReqVOMono.flatMap(quoteReqVO -> {
            List<FetchPlanRequestVO> fetchPlanRequestVOList = new ArrayList<>();
            return vendorProductMngtService.getProducts(queryMap, headers).map(productRes -> {
                productRes.get(ITEMS).forEach(product -> {
                    if (Objects.isNull(quoteReqVO.getProductCode())) {
                        System.out.println("product code and ID ::\t" + product.get(CODE) + "\t" + product.get(ID).asText());
                        fetchPlanRequestVOList.add(formFetchPlanRequest(quoteReqVO, product.get(ID).asText(), product.get("category_id").asText(), product.get(CODE).asText()));
                    } else if (Objects.nonNull(quoteReqVO.getProductCode()) && quoteReqVO.getProductCode().name().equalsIgnoreCase(product.get(CODE).asText())) {
                        fetchPlanRequestVOList.add(formFetchPlanRequest(quoteReqVO, product.get(ID).asText(), product.get("category_id").asText(), product.get(CODE).asText()));
                    }
                });
                System.out.println("\n\n list request :::" + fetchPlanRequestVOList);
                return fetchPlanRequestVOList;
            }).doOnError(e -> {
                System.out.println("Getting Exception while doing get products call :: " + e.getMessage());
                Mono.error(new ServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
            });
        }).doOnError(e -> {
            System.out.println("\n\nInvalid request::: " + e.getMessage());
            Mono.error(new ServiceException(e.getMessage(), HttpStatus.BAD_REQUEST));
        });
    }

    private Mono<FetchQuoteResponseVO> populateResponse(FetchPlanRequestVO fetchPlanRequestVO, JsonNode responseJson, JsonNode addonJson, CategoryAttributeDTO categoryAttributeDTO, Map<String, List<String>> headers) {
        FetchQuoteResponseVO fetchQuoteResponseVO = new FetchQuoteResponseVO();
        fetchQuoteResponseVO.setProductCode(fetchPlanRequestVO.getProductCode());
        try {
            if (JsonUtils.isValidField(responseJson, OUTPUT_QUOTE_PARAMS)
                    && JsonUtils.isValidField(responseJson.get(OUTPUT_QUOTE_PARAMS), JsonUtils.JSON_ARRAY_ITEMS_KEY)
                    && JsonUtils.isValidIndex(responseJson.get(OUTPUT_QUOTE_PARAMS).get(JsonUtils.JSON_ARRAY_ITEMS_KEY), 0)
                    && JsonUtils.isValidField(responseJson.get(OUTPUT_QUOTE_PARAMS).get(JsonUtils.JSON_ARRAY_ITEMS_KEY).get(0), "attributes")) {
                List<QuoteResVO> quotes = new ArrayList<>();
                String categoryId = responseJson.get("categoryId").asText();
//                return categoryAttributeDTOMono.flatMap(categoryAttributeDTO -> {
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
//                            quoteVO.setTenure(attributesJson.get("AI-POLICY_TERM").get("value").asInt());
                        MoneyVO basePremium = new MoneyVO(attributesJson.get("AH-BASE_PREMIUM").get("value").asDouble(), CurrencyCode.INR);
                        MoneyVO totalPremium = new MoneyVO(attributesJson.get("AH-PREMIUM_AMOUNT").get("value").asDouble(), CurrencyCode.INR);
                        MoneyVO premiumWithoutDiscount = null;
                        if (JsonUtils.isValidField(attributesJson, "AH-PREMIUM_WITHOUT_DISCOUNT")) {
                            premiumWithoutDiscount = new MoneyVO(attributesJson.get("AH-PREMIUM_WITHOUT_DISCOUNT").get("value").asDouble(), CurrencyCode.INR);
                        }
                        Map<String, MoneyVO> addOns = getAddonMap(attributesJson, addonPremiumAttributes, categoryAttributeDTO.getCategoryAttributeCodeMap());
                        PremiumVO premiumVO = new PremiumVO(basePremium, premiumWithoutDiscount, addOns, totalPremium, VENDOR_TAX_PERCENTAGE);
                        quoteVO.setPremiumWithoutTax(premiumVO);
                        if (fetchPlanRequestVO.getTenure() == attributesJson.get("AI-POLICY_TERM").get("value").asInt())
                            quotes.add(quoteVO);

                    });
                    fetchQuoteResponseVO.setEnquiryId(responseJson.get("enquiryId").asText());
                    fetchQuoteResponseVO.setTenure(fetchPlanRequestVO.getTenure());
                    fetchQuoteResponseVO.setItems(quotes);
                    return Mono.just(fetchQuoteResponseVO);
                }
                fetchQuoteResponseVO.setErrorMsg("Unable to get Quotes.");
                return Mono.just(fetchQuoteResponseVO);
//                }).doOnError(e -> {
//                    fetchQuoteResponseVO.setErrorMsg("Unable to get Quotes." + e.getMessage());
//                    Mono.just(fetchQuoteResponseVO);
//                    return;
//                });
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

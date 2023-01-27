package com.arka.streamingserv.utils;

import com.arka.helperlib.constants.security.Privilege;
import com.arka.helperlib.utils.CriteriaUtils;
import com.arka.helperlib.utils.JsonUtils;
import com.arka.helperlib.utils.secutity.HeaderUtils;
import com.arka.streamingserv.dto.*;
import com.arka.streamingserv.service.VendorProductMngtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CategoryConfig {

    @Lazy
    @Autowired
    private VendorProductMngtService vendorProductMngtService;

    @PostConstruct
    public Mono<CategoryDTO> getCategoryDetails() {

        Map<String, List<String>> queryMap = new HashMap<>();
        queryMap.put(CriteriaUtils.NO_OF_RECORDS, Collections.singletonList(CriteriaUtils.ALL_RECORDS_VALUE));
        return vendorProductMngtService.getCategories(queryMap, new HashMap<>())
                .flatMap(categoriesJson-> {
                    if(JsonUtils.isValidField(categoriesJson,JsonUtils.JSON_ARRAY_ITEMS_KEY)
                            && JsonUtils.isValidIndex(categoriesJson.get(JsonUtils.JSON_ARRAY_ITEMS_KEY),0)) {
                        Map<String, String> categoryCodeMap = new HashMap<>();
                        Map<String, String> categoryIdMap = new HashMap<>();
                        categoriesJson.get(JsonUtils.JSON_ARRAY_ITEMS_KEY).forEach(eachCategory->{
                            if(JsonUtils.isValidField(eachCategory, "_id") && JsonUtils.isValidField(eachCategory,"code")) {
                                categoryCodeMap.put(eachCategory.get("code").asText(), eachCategory.get("_id").asText());
                                categoryIdMap.put(eachCategory.get("_id").asText(),eachCategory.get("code").asText());
                            }
                        });
                        if(!CollectionUtils.isEmpty(categoryCodeMap)) {
                            CategoryDTO categoryDTO = new CategoryDTO();
                            categoryDTO.setCategoryCodeMap(categoryCodeMap);
                            categoryDTO.setCategoryIdMap(categoryIdMap);
                            return Mono.just(categoryDTO);
                        }
                    }
                    return Mono.empty();
                });

    }

    public Mono<CategoryAttributeDTO> getCategoryAttributesDetails(String categoryId) {

        Map<String, List<String>> categoryAttributeQueryMap = new HashMap<>();
        categoryAttributeQueryMap.put("parent", Collections.singletonList(String.valueOf(true)));
        categoryAttributeQueryMap.put(CriteriaUtils.NO_OF_RECORDS, Collections.singletonList(CriteriaUtils.ALL_RECORDS_VALUE));
        Map<String, List<String>> headerMap = new HashMap<>();
        HeaderUtils.setPrivilegeInHeaders(Collections.singletonList(Privilege.CATALOG_READ), headerMap);
        return vendorProductMngtService.getCategoryAttributesById(categoryId, categoryAttributeQueryMap, headerMap)
                .flatMap(categoryAttributesJson-> {
                    if(JsonUtils.isValidField(categoryAttributesJson,JsonUtils.JSON_ARRAY_ITEMS_KEY)
                            && JsonUtils.isValidIndex(categoryAttributesJson.get(JsonUtils.JSON_ARRAY_ITEMS_KEY),0)) {
                        CategoryAttributeDTO categoryAttributeDTO = new CategoryAttributeDTO();
                        Map<String, AttributeDTO> attributesAliasMap = new HashMap<>();
                        Map<String, AttributeDTO> attributesCodeMap = new HashMap<>();
                        categoryAttributesJson.get(JsonUtils.JSON_ARRAY_ITEMS_KEY).forEach(eachCategoryAttribute->{
                            if(JsonUtils.isValidField(eachCategoryAttribute, "_id")
                                    && JsonUtils.isValidField(eachCategoryAttribute,"code")
                                    && JsonUtils.isValidField(eachCategoryAttribute,"alias_name")) {
                                String aliasName = eachCategoryAttribute.get("alias_name").asText();
                                AttributeDTO attributeDTO = new AttributeDTO();
                                attributeDTO.setId(eachCategoryAttribute.get("_id").asText());
                                attributeDTO.setCode(eachCategoryAttribute.get("code").asText());
                                attributeDTO.setAlias(aliasName);
                                attributeDTO.setDisplayName(eachCategoryAttribute.get("display_label").asText());
                                attributesAliasMap.put(aliasName, attributeDTO);
                                attributesCodeMap.put(eachCategoryAttribute.get("code").asText(), attributeDTO);
                            }
                        });
                        if(!CollectionUtils.isEmpty(attributesAliasMap)) {
                            categoryAttributeDTO.setCategoryAttributeMap(attributesAliasMap);
                            categoryAttributeDTO.setCategoryAttributeCodeMap(attributesCodeMap);
                            return Mono.just(categoryAttributeDTO);
                        }
                    }
                    return Mono.empty();
                });
    }

    public Mono<ProductDTO> getProductDetails() {

        Map<String, List<String>> queryMap = new HashMap<>();
        queryMap.put(CriteriaUtils.NO_OF_RECORDS, Collections.singletonList(CriteriaUtils.ALL_RECORDS_VALUE));
        return vendorProductMngtService.getProducts(queryMap, new HashMap<>())
                .flatMap(productsJson-> {
                    if(JsonUtils.isValidField(productsJson,JsonUtils.JSON_ARRAY_ITEMS_KEY)
                            && JsonUtils.isValidIndex(productsJson.get(JsonUtils.JSON_ARRAY_ITEMS_KEY),0)) {
                        Map<String, String> productCodeMap = new HashMap<>();
                        Map<String, String> productIdMap = new HashMap<>();
                        productsJson.get(JsonUtils.JSON_ARRAY_ITEMS_KEY).forEach(eachProduct->{
                            if(JsonUtils.isValidField(eachProduct, "_id") && JsonUtils.isValidField(eachProduct,"code")) {
                                productCodeMap.put(eachProduct.get("code").asText(), eachProduct.get("_id").asText());
                                productIdMap.put(eachProduct.get("_id").asText(),eachProduct.get("code").asText());
                            }
                        });
                        if(!CollectionUtils.isEmpty(productCodeMap)) {
                            ProductDTO productDTO = new ProductDTO();
                            productDTO.setProductCodeMap(productCodeMap);
                            productDTO.setProductIdMap(productIdMap);
                            return Mono.just(productDTO);
                        }
                    }
                    return Mono.empty();
                });

    }

    public Mono<ProductFeatureDTO> getProductFeatures(Map<String, List<String>> queryParams, Map<String, List<String>> headers) {

        return vendorProductMngtService.getProductFeaturesV2(queryParams, headers)
                .flatMap(productsJson-> {
                    if(JsonUtils.isValidField(productsJson,JsonUtils.JSON_ARRAY_ITEMS_KEY)
                            && JsonUtils.isValidIndex(productsJson.get(JsonUtils.JSON_ARRAY_ITEMS_KEY),0)) {
                        Map<String, FeatureDTO> productIdsFeaturesMap = new HashMap<>();
                        Map<String, FeatureDTO> productCodesFeaturesMap = new HashMap<>();
                        productsJson.get(JsonUtils.JSON_ARRAY_ITEMS_KEY).forEach(eachProductFeatures->{
                            FeatureDTO featureDTO = JsonUtils.fromJson(eachProductFeatures, FeatureDTO.class);
                            productIdsFeaturesMap.put(eachProductFeatures.get("productId").asText(), featureDTO);
                            productCodesFeaturesMap.put(eachProductFeatures.get("productCode").asText(), featureDTO);
                        });
                        if(!CollectionUtils.isEmpty(productIdsFeaturesMap)) {
                            ProductFeatureDTO productFeatureDTO = new ProductFeatureDTO();
                            productFeatureDTO.setProductIdsFeaturesMap(productIdsFeaturesMap);
                            productFeatureDTO.setProductCodesFeaturesMap(productCodesFeaturesMap);
                            return Mono.just(productFeatureDTO);
                        }
                    }
                    return Mono.empty();
                });

    }


}

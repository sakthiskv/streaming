package com.arka.streamingserv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FeatureDTO {

    private String productId;

    private String productCode;

    private Map<String, Map<String, List<ProductFeatureAttributeDTO>>> productFeatures;

}

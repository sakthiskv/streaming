package com.arka.streamingserv.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ProductFeatureDTO {

    private Map<String, FeatureDTO> productIdsFeaturesMap;

    private Map<String, FeatureDTO> productCodesFeaturesMap;

}

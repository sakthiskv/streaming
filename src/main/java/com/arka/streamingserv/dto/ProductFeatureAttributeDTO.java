package com.arka.streamingserv.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductFeatureAttributeDTO {

    private List<ProductFeatureAttributeValueDTO> values;

    private String description;

}

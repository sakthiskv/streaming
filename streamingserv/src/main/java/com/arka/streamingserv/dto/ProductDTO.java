package com.arka.streamingserv.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ProductDTO {

    public Map<String, String> productCodeMap;

    public Map<String, String> productIdMap;

}

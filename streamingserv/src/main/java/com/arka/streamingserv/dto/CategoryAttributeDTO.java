package com.arka.streamingserv.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CategoryAttributeDTO {

    private Map<String, AttributeDTO> categoryAttributeMap;

    private Map<String, AttributeDTO> categoryAttributeCodeMap;

}

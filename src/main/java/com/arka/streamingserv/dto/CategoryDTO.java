package com.arka.streamingserv.dto;

import lombok.Data;

import java.util.Map;

@Data
public class CategoryDTO {

    public Map<String, String> categoryCodeMap;

    public Map<String, String> categoryIdMap;

}

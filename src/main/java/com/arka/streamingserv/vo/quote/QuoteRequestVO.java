package com.arka.streamingserv.vo.quote;

import lombok.Data;

@Data
public class QuoteRequestVO {

    private String enquiryId;

    private int tenure;

    private String productId;

    private String categoryId;

    private String productCode;
}

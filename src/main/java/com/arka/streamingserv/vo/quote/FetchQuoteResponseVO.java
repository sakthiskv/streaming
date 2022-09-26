package com.arka.streamingserv.vo.quote;

import lombok.Data;

import java.util.List;

@Data
public class FetchQuoteResponseVO {

    private String enquiryId;

    private int tenure;

    private String errorMsg;

    private String productCode;

    private List<QuoteResVO> items;

}

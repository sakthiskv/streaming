package com.arka.streamingserv.vo.quote;

import lombok.Data;

import java.util.List;

@Data
public class FetchQuoteVO {

    private String enquiryId;

    private List<QuoteVO> items;

}

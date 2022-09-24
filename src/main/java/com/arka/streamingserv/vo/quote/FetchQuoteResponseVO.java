package com.arka.streamingserv.vo.quote;

import lombok.Data;

import java.util.List;

@Data
public class FetchQuoteResponseVO {

    private String enquiryId;

    private List<QuoteResVO> items;

}

package com.arka.streamingserv.service;

import com.arka.streamingserv.vo.quote.FetchQuoteReqVO;
import com.arka.streamingserv.vo.quote.FetchQuoteVO;
import com.arka.streamingserv.vo.quote.QuoteVO;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface QuoteService {

    Flux<QuoteVO> fetchQuotes(Map<String, String> headers, Mono<FetchQuoteReqVO> fetchQuoteReqVO);


}

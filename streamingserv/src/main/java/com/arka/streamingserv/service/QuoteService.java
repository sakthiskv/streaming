package com.arka.streamingserv.service;

import com.arka.streamingserv.vo.quote.QuoteReqVO;
import com.arka.streamingserv.vo.quote.QuoteResVO;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface QuoteService {

    Flux<QuoteResVO> fetchQuotes(Map<String, String> headers, Mono<QuoteReqVO> fetchQuoteReqVO);


}

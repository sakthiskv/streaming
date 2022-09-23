package com.arka.streamingserv.handlers;

import com.arka.streamingserv.service.QuoteService;
import com.arka.streamingserv.vo.quote.FetchQuoteReqVO;
import com.arka.streamingserv.vo.quote.FetchQuoteVO;
import com.arka.streamingserv.vo.quote.QuoteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.ServerResponse.notFound;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class QuoteHandler {

    @Autowired
    QuoteService quoteService;

    public Mono<ServerResponse> getAllQuotes(ServerRequest serverRequest) {
        Map<String, String> headers = serverRequest.headers().asHttpHeaders().toSingleValueMap();
        Mono<FetchQuoteReqVO> fetchQuoteReqVOMono = serverRequest.bodyToMono(FetchQuoteReqVO.class);
        System.out.println("came inside sakthi");
        Flux<QuoteVO> quoteVOFlux = quoteService.fetchQuotes(headers, fetchQuoteReqVOMono);
        return ok().contentType(APPLICATION_JSON).body(quoteVOFlux, QuoteVO.class);
    }
}

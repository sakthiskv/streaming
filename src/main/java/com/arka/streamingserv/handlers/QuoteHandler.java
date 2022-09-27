package com.arka.streamingserv.handlers;

import com.arka.streamingserv.service.QuoteService;
import com.arka.streamingserv.vo.quote.QuoteReqVO;
import com.arka.streamingserv.vo.quote.FetchQuoteResponseVO;
import com.arka.streamingserv.vo.quote.QuoteResVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class QuoteHandler {

    @Autowired
    QuoteService quoteService;

    public Mono<ServerResponse> getAllQuotes(ServerRequest serverRequest) {
        Map<String, String> headers = serverRequest.headers().asHttpHeaders().toSingleValueMap();
        Mono<QuoteReqVO> fetchQuoteReqVOMono = serverRequest.bodyToMono(QuoteReqVO.class);
//        Flux<FetchQuoteResponseVO> quoteVOFlux = quoteService.fetchQuotes(headers, fetchQuoteReqVOMono);
//        return ok().contentType(MediaType.APPLICATION_NDJSON)
//                .body(quoteVOFlux, FetchQuoteResponseVO.class);
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_STREAM_JSON)
//                .header("Content-Disposition", "inline")
                .body(
                        Flux.range(1,5)
                                .delayElements(Duration.ofSeconds(2))
                                .map(value-> new QuoteResVO()), QuoteResVO.class
                );
    }
}

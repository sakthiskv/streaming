package com.arka.streamingserv.router;

import com.arka.streamingserv.handlers.QuoteHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class QuoteServiceRouter {

    @Autowired
    QuoteHandler quoteHandler;

    @Bean
    public RouterFunction<ServerResponse> QuoteServiceRouterFucntion() {
        return RouterFunctions.route()
                .POST("/v1/streamingserv/fetch-quotes", accept(APPLICATION_JSON), quoteHandler::getAllQuotes)
                .build();
    }

}

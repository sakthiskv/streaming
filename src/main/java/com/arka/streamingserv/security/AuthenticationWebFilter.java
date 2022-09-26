package com.arka.streamingserv.security;

import com.arka.helperlib.Exception.ServiceException;
import com.arka.helperlib.constants.security.HeaderParams;
import com.arka.helperlib.constants.security.SecurityContext;
import com.arka.helperlib.utils.JsonUtils;
import com.arka.streamingserv.constants.ErrorMessageKey;
import com.arka.streamingserv.utils.ErrorUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthenticationWebFilter implements WebFilter {

    @Autowired
    private JWTProvider jwtProvider;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        SecurityContext securityContext = getSecurityContext(exchange);
        Authentication authentication = getAuthentication(securityContext);
        ReactiveSecurityContextHolder.getContext().map(reactiveSecurityContext -> {
            reactiveSecurityContext.setAuthentication(authentication);
            return Mono.just(reactiveSecurityContext);
        });
        ServerHttpRequest mutateRequest = exchange.getRequest().mutate()
                .header(HeaderParams.X_SEC_CTX.getValue(),JsonUtils.stringify(securityContext))
                .header(HeaderParams.X_CREATED_IP.getValue(),exchange.getRequest().getRemoteAddress().getHostName())
                .build();
        ServerWebExchange mutateServerWebExchange = exchange.mutate().request(mutateRequest).build();
        return chain.filter(mutateServerWebExchange);
    }

    private SecurityContext getSecurityContext(ServerWebExchange serverWebExchange) throws ServiceException {
        String seqCtx = serverWebExchange.getRequest().getHeaders().containsKey(HeaderParams.X_SEC_CTX.getValue())
                        ? serverWebExchange.getRequest().getHeaders().getFirst(HeaderParams.X_SEC_CTX.getValue())
                        : null;
        if(Objects.nonNull(seqCtx)) {
            JsonNode secJson = JsonUtils.toJsonIfNotNull(seqCtx);
            return Optional.ofNullable(secJson)
                    .map(secJsonNode ->JsonUtils.fromJson(secJsonNode,SecurityContext.class))
                    .orElseThrow(() -> {
                        ObjectNode errorJson = JsonUtils.newJsonObject();
                        errorJson.put(JsonUtils.ERROR, ErrorMessageKey.INVALID_SECURITY_CONTEXT.getValue());
                        return new ServiceException(ErrorUtils.formUnauthorizedErrorVO(errorJson), HttpStatus.UNAUTHORIZED);
                    });
        }
        String token = jwtProvider.resolveToken(serverWebExchange.getRequest());
        if(Objects.nonNull(token) && jwtProvider.validateToken(token)) {
            return jwtProvider.getSecurityContext(token);
        }
        return SecurityContext.builder().build();
    }

    private Authentication getAuthentication(SecurityContext securityContext) {
        List<String> privileges = Optional.ofNullable(securityContext.getPrivileges()).orElseGet(ArrayList::new);
        List<SimpleGrantedAuthority> grantedAuthorities = privileges.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        System.out.println("grantedAuthorities:" + grantedAuthorities);
        return new UsernamePasswordAuthenticationToken(securityContext.getSubject(), "", grantedAuthorities);
    }

}

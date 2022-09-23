package com.arka.streamingserv.security;

import com.arka.helperlib.Exception.ServiceException;
import com.arka.helperlib.constants.security.SecurityContext;
import com.arka.helperlib.utils.JsonUtils;
import com.arka.streamingserv.constants.ErrorMessageKey;
import com.arka.streamingserv.utils.ErrorUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@Component
@PropertySource("classpath:/security.properties")
public class JWTProvider {

    @Value("${user.auth.signature.key}")
    private String authSignatureKey;

    private static final String BEARER = "Bearer ";
    private static final Predicate<String> matchBearerLength = authValue -> authValue.length() > BEARER.length();
    private static final Function<String, Mono<String>> isolateBearerValue = authValue -> Mono.justOrEmpty(authValue.substring(BEARER.length()));

    private static String PRIVILEGES = "privileges";

    private static String USER_GROUP_ID = "userGroupId";

    private static String USER_GROUP_NAME = "group_name";

    public static final String USER_ID = "userId";

    private static String AUTHORIZATION = "Authorization";

    public SecurityContext getSecurityContext(String token) {
        Claims claims = Jwts.parser().setSigningKey(authSignatureKey).parseClaimsJws(token).getBody();
        SecurityContext securityContext = SecurityContext.builder().build();
        if(Objects.nonNull(claims)) {
            return securityContext.toBuilder()
                    .subject(claims.get(USER_ID).toString())
                    .privileges(claims.get(PRIVILEGES, List.class))
                    .build();
        }
        return securityContext;
    }

    public String resolveToken(ServerHttpRequest serverHttpRequest) {
        String bearerToken = serverHttpRequest.getHeaders().containsKey(AUTHORIZATION)
                ? Optional.ofNullable(serverHttpRequest.getHeaders().get(AUTHORIZATION))
                .map(secHeaders-> secHeaders.get(0))
                .orElse(null)
                : null;
        if (bearerToken != null && bearerToken.startsWith(BEARER)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(authSignatureKey).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            ObjectNode errorJson = JsonUtils.newJsonObject();
            errorJson.put(JsonUtils.ERROR, ErrorMessageKey.AUTH_TOKEN_EXPIRED.getValue());
//            throw new ServiceException(ErrorUtils.formUnauthorizedErrorVO(errorJson), HttpStatus.UNAUTHORIZED);
        } catch(JwtException e) {
            ObjectNode errorJson = JsonUtils.newJsonObject();
            errorJson.put(JsonUtils.ERROR, ErrorMessageKey.INVALID_TOKEN.getValue());
//            throw new ServiceException(ErrorUtils.formUnauthorizedErrorVO(errorJson), HttpStatus.UNAUTHORIZED);
        }
        return true;
    }

}

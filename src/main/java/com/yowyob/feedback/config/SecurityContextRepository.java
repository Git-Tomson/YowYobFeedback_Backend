package com.yowyob.feedback.config;

import com.yowyob.feedback.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * Custom security context repository for JWT-based authentication.
 * Extracts and validates JWT tokens from request headers.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtService jwt_service;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String auth_header = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (auth_header != null && auth_header.startsWith(BEARER_PREFIX)) {
            String token = auth_header.substring(BEARER_PREFIX.length());

            return jwt_service.validateTokenAndExtractUserId(token)
                    .map(user_id -> {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user_id,
                                        null,
                                        Collections.emptyList()
                                );
                        return (SecurityContext) new SecurityContextImpl(authentication);
                    })
                    .doOnError(error -> log.error("JWT validation failed: {}", error.getMessage()))
                    .onErrorReturn(new SecurityContextImpl());
        }

        return Mono.empty();
    }
}

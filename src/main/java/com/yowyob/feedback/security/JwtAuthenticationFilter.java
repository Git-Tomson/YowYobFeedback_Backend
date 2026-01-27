package com.yowyob.feedback.security;

import com.yowyob.feedback.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import org.springframework.core.annotation.Order;

/**
 * JWT authentication filter for validating tokens in requests.
 * Skips authentication for public endpoints.
 *
 * @author Thomas Djotio Ndié
 * @since 2026-01-27
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(-100) // Execute before Spring Security filters
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtService jwt_service;

    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    // Public paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/",
            "/api/v1/health",
            "/v1/api-docs",
            "/v3/api-docs",
            "/swagger-ui",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            log.debug("Skipping JWT validation for public path: {}", path);
            return chain.filter(exchange);
        }

        String auth_header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (auth_header == null || !auth_header.startsWith(BEARER_PREFIX)) {
            log.info("No JWT token found for path: {}", path);
            return chain.filter(exchange);
        }

        try {
            String token = auth_header.substring(BEARER_PREFIX_LENGTH);
            String user_email = jwt_service.extractUsername(token);

            if (user_email != null && jwt_service.isTokenValid(token, user_email)) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                user_email,
                                null,
                                new ArrayList<>()
                        );

                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            }
        } catch (Exception e) {
            log.error("JWT validation error for path {}: {}", path, e.getMessage());
        }

        return chain.filter(exchange);
    }

    /**
     * Checks if the given path is a public endpoint.
     *
     * @param path the request path
     * @return true if path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}

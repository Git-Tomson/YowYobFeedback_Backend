package com.yowyob.feedback.security;

import com.yowyob.feedback.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;

/**
 * JWT authentication filter for WebFlux.
 * Intercepts requests and validates JWT tokens.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@Slf4j  //Create a log variable
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private final JwtService jwt_service;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.info("Passage dans le filtre pour le chemin : {}", path);

        // 1. Gestion des chemins publics
        if (isPublicPath(path)) {
            log.info("Chemin considéré comme PUBLIC : {}", path);
            return chain.filter(exchange);
        }

        // 2. Extraction du header Authorization
        String auth_header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (auth_header == null || !auth_header.startsWith(BEARER_PREFIX)) {
            log.warn("Tentative d'accès sans token valide sur : {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = auth_header.substring(BEARER_PREFIX.length());

        // 3. Validation réactive et chaînage
        return jwt_service.validateTokenAndExtractUserId(token)
                .flatMap(userId -> {
                    // Si le token est valide, on crée l'objet Authentication
                    // On utilise userId (UUID) comme principal
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.emptyList()
                    );

                    log.info("Authentification réussie pour l'utilisateur : {}", userId);

                    // On continue la chaîne en injectant l'authentification dans le contexte réactif
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                })
                // 4. Gestion du cas où le token est invalide (le Mono est vide)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("JWT invalide ou expiré pour le chemin : {}", path);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }))
                // 5. Gestion des erreurs techniques (ex: signature corrompue, erreur de parsing)
                .onErrorResume(e -> {
                    log.error("Erreur lors de la validation du JWT : {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    /**
     * Checks if the path is public (doesn't require authentication).
     *
     * @param path request path
     * @return true if path is public
     */
    private boolean isPublicPath(String path) {
        return path.equals("/api/v1/auth/register") ||
                path.equals("/api/v1/auth/login") ||
                path.startsWith("/api/v1/auth/password-reset") ||
                path.equals("/api/v1/auth/2fa/verify") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/actuator");
    }
}

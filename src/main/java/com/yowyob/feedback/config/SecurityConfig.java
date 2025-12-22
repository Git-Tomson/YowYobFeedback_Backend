package com.yowyob.feedback.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the application.
 * Configures password encoding and web security rules.
 * Uses BCrypt for password hashing.
 * Disables CSRF for REST API usage.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 1.0
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String AUTH_PATH_PATTERN = "/api/v1/auth/**";
    private static final String API_DOCS_PATH_PATTERN = "/v3/api-docs/**";
    private static final String SWAGGER_UI_PATH_PATTERN = "/swagger-ui/**";
    private static final String SWAGGER_HTML_PATH = "/swagger-ui.html";
    private static final String ACTUATOR_PATH_PATTERN = "/actuator/**";

    /**
     * Creates BCrypt password encoder bean.
     * BCrypt is a secure hashing algorithm for passwords.
     *
     * @return PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures security filter chain for WebFlux.
     * <p>
     * Configuration:
     * - CSRF disabled for REST API
     * - Authentication endpoints are public
     * - API documentation endpoints are public
     * - Actuator endpoints are public
     * - All other endpoints require authentication
     *
     * @param http the ServerHttpSecurity to configure
     * @return SecurityWebFilterChain configured security chain
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(AUTH_PATH_PATTERN).permitAll()
                        .pathMatchers(API_DOCS_PATH_PATTERN, SWAGGER_UI_PATH_PATTERN,
                                SWAGGER_HTML_PATH).permitAll()
                        .pathMatchers(ACTUATOR_PATH_PATTERN).permitAll()
                        .anyExchange().authenticated()
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}

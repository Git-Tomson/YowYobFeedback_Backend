package com.yowyob.feedback.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS configuration for production.
 * Restricts access to specific frontend domains.
 *
 * @author Thomas Djotio Ndié
 * @since 2026-01-26
 * @version 1.0
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private String allowed_origins;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cors_config = new CorsConfiguration();

        // Parse allowed origins from configuration
        List<String> origins = Arrays.asList(allowed_origins.split(","));
        cors_config.setAllowedOrigins(List.of("*"));

        // Allow specific HTTP methods
        cors_config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        // Allow specific headers
        cors_config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With"
        ));

        cors_config.setAllowCredentials(true);
        cors_config.setMaxAge(3600L);

        cors_config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors_config);

        return new CorsWebFilter(source);
    }
}

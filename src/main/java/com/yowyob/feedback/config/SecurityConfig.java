package com.yowyob.feedback.config;

//import com.yowyob.feedback.security.JwtAuthenticationFilter;
import com.yowyob.feedback.security.SecurityContextRepository;
import com.yowyob.feedback.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the application.
 * Configures password encoding and web security rules with JWT authentication.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String AUTH_PATH_PATTERN = "/api/v1/auth/**";
    private static final String API_DOCS_PATH_PATTERN = "/v1/api-docs/**";
    private static final String SWAGGER_UI_PATH_PATTERN = "/swagger-ui/**";
    private static final String SWAGGER_HTML_PATH = "/swagger-ui.html";
    private static final String ACTUATOR_PATH_PATTERN = "/actuator/**";
    private static final String HEALTH_CHECKS_PATTERN = "/api/v1/health";
    private final SecurityContextRepository security_context_repository;

    /**
     * Creates BCrypt password encoder bean.
     *
     * @return PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures security filter chain for WebFlux with JWT authentication.
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
                        .pathMatchers(ACTUATOR_PATH_PATTERN,
                                HEALTH_CHECKS_PATTERN).permitAll()
                        .anyExchange().authenticated()
                )
                .securityContextRepository(security_context_repository)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

}

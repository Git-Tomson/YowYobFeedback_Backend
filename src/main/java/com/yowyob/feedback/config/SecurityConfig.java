package com.yowyob.feedback.config;

import com.yowyob.feedback.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security configuration for the application.
 * Configures password encoding, JWT authentication, and web security rules.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 2.0
 */
@Configuration //Tell Spring: "Read this class at startup because it contains instructions to configure the application."
@EnableWebFluxSecurity//Enables security support for reactive applications (WebFlux). Without this, our settings will be ignored.
@RequiredArgsConstructor//automatically generates a constructor for all variables marked final
public class SecurityConfig {

    private static final String AUTH_PATH_PATTERN = "/api/v1/auth/register";
    private static final String LOGIN_PATH_PATTERN = "/api/v1/auth/login";
    private static final String PASSWORD_RESET_PATH_PATTERN = "/api/v1/auth/password-reset/**";
    private static final String TWO_FA_VERIFY_PATH_PATTERN = "/api/v1/auth/2fa/verify";
    private static final String API_DOCS_PATH_PATTERN = "/v3/api-docs/**";
    private static final String SWAGGER_UI_PATH_PATTERN = "/swagger-ui/**";
    private static final String SWAGGER_HTML_PATH = "/swagger-ui.html";
    private static final String ACTUATOR_PATH_PATTERN = "/actuator/**";

    private final JwtAuthenticationFilter jwt_authentication_filter;

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
     * - Public endpoints: register, login, password reset, 2FA verify, docs, actuator
     * - All other endpoints require JWT authentication
     * - JWT filter added before default authentication
     *
     * @param http the ServerHttpSecurity to configure
     * @return SecurityWebFilterChain configured security chain
     */
    /*
    @Primary // Force Spring à utiliser ce Bean en priorité
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(AUTH_PATH_PATTERN, LOGIN_PATH_PATTERN,
                                PASSWORD_RESET_PATH_PATTERN, TWO_FA_VERIFY_PATH_PATTERN,
                                API_DOCS_PATH_PATTERN, SWAGGER_UI_PATH_PATTERN,
                                SWAGGER_HTML_PATH, ACTUATOR_PATH_PATTERN).permitAll()
                        .anyExchange().authenticated()
                )
                // On place le filtre JWT AVANT l'authentification
                .addFilterBefore(jwt_authentication_filter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
    @Bean
    public MapReactiveUserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails user = User.builder()
                .username("system_internal")
                .password(passwordEncoder.encode("internal_password_not_used"))
                .roles("SYSTEM")
                .build();
        return new MapReactiveUserDetailsService(user);
    }
    */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(AUTH_PATH_PATTERN).permitAll()
                        .pathMatchers(LOGIN_PATH_PATTERN).permitAll()
                        .pathMatchers(PASSWORD_RESET_PATH_PATTERN).permitAll()
                        .pathMatchers(TWO_FA_VERIFY_PATH_PATTERN).permitAll()
                        .pathMatchers(API_DOCS_PATH_PATTERN, SWAGGER_UI_PATH_PATTERN,
                                SWAGGER_HTML_PATH).permitAll()
                        .pathMatchers(ACTUATOR_PATH_PATTERN).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(jwt_authentication_filter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}

package com.yowyob.feedback.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Utility class for security-related operations.
 * Provides helper methods to extract authenticated user information.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Slf4j
public final class SecurityUtil {

    private SecurityUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Extracts the authenticated user ID from the security context.
     *
     * @return Mono<UUID> the authenticated user ID
     * @throws IllegalStateException if no authentication is found
     */
    public static Mono<UUID> getAuthenticatedUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(authentication -> {
                    Object principal = authentication.getPrincipal();
                    if (principal instanceof UUID) {
                        return (UUID) principal;
                    }
                    throw new IllegalStateException("Invalid authentication principal type");
                })
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("No authentication found in security context")));
    }
}

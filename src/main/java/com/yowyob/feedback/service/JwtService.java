package com.yowyob.feedback.service;

import com.yowyob.feedback.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service handling JSON Web Token (JWT) generation and validation.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private static final String SECRET_KEY_STRING = "your_very_secret_and_long_key_for_hmac_signature_256_bits";
    private static final long DEFAULT_EXPIRATION_TIME = 86400000; // 24 hours in ms
    private static final String USER_ID_CLAIM = "user_id";

    /**
     * Generates a signed JWT for a given user.
     *
     * @param user the authenticated user entity
     * @return a compact URL-safe JWT string
     */
    public String generateToken(AppUser user) {
        Map<String, Object> extra_claims = new HashMap<>();
        extra_claims.put("role", user.getUser_type());
        extra_claims.put(USER_ID_CLAIM, user.getUser_id().toString());

        long current_time = System.currentTimeMillis();
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(extra_claims)
                .subject(user.getEmail() != null ? user.getEmail() : user.getContact())
                .issuedAt(new Date(current_time))
                .expiration(new Date(current_time + DEFAULT_EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }

    /**
     * Validates a JWT token and extracts the user ID.
     *
     * @param token the JWT token
     * @return Mono<UUID> the user ID from the token
     */
    public Mono<UUID> validateTokenAndExtractUserId(String token) {
        return Mono.fromCallable(() -> {
            SecretKey key = Keys.hmacShaKeyFor(
                    SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String user_id_string = claims.get(USER_ID_CLAIM, String.class);
            return UUID.fromString(user_id_string);
        });
    }

    /**
     * Extracts user ID from token without full validation (for testing).
     *
     * @param token the JWT token
     * @return UUID the user ID
     */
    public UUID extractUserIdUnsafe(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
                SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String user_id_string = claims.get(USER_ID_CLAIM, String.class);
        return UUID.fromString(user_id_string);
    }
}

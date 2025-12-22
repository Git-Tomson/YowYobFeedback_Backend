package com.yowyob.feedback.service;

import com.yowyob.feedback.entity.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Service handling JSON Web Token (JWT) generation and validation.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-12-22
 * @version 0.1
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    // Constante en majuscules avec snake_case selon la charte [cite: 10]
    private static final String SECRET_KEY_STRING = "your_very_secret_and_long_key_for_hmac_signature_256_bits";
    private static final long DEFAULT_EXPIRATION_TIME = 3600000; // 1 hour in ms

    /**
     * Generates a signed JWT for a given user.
     *
     * @param user the authenticated user entity
     * @return a compact URL-safe JWT string
     */
    public String generateToken(AppUser user) {
        // Variables en snake_case selon la charte
        Map<String, Object> extra_claims = new HashMap<>();
        extra_claims.put("role", user.getUser_type());

        long current_time = System.currentTimeMillis();
        // Utilisation de SecretKey pour respecter les standards de sécurité
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(extra_claims)
                .subject(user.getEmail())
                .issuedAt(new Date(current_time))
                .expiration(new Date(current_time + DEFAULT_EXPIRATION_TIME))
                .signWith(key)
                .compact();
    }
}

package com.yowyob.feedback.service;

import com.yowyob.feedback.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service handling JSON Web Token (JWT) generation and validation.
 * Provides methods for creating, parsing, and validating JWTs.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-12-22
 * @version 0.2
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.security.jwt.secret}")
    private String secret_key_string;

    @Value("${app.security.jwt.expiration}")
    private long expiration_time;
    /**
     * Generates a signed JWT for a given user.
     *
     * @param user the authenticated user entity
     * @return a compact URL-safe JWT string
     */
    public String generateToken(AppUser user) {
        Map<String, Object> extra_claims = new HashMap<>();
        extra_claims.put("role", user.getUser_type());
        extra_claims.put("user_id", user.getUser_id().toString());

        long current_time = System.currentTimeMillis();
        SecretKey key = Keys.hmacShaKeyFor(secret_key_string.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .claims(extra_claims)
                .subject(user.getEmail() != null ? user.getEmail() : user.getContact())
                .issuedAt(new Date(current_time))
                .expiration(new Date(current_time + expiration_time))
                .signWith(key)
                .compact();
    }

    /**
     * Extracts username (subject) from JWT token.
     *
     * @param token the JWT token
     * @return username
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts expiration date from JWT token.
     *
     * @param token the JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from JWT token.
     *
     * @param token the JWT token
     * @param claims_resolver function to extract specific claim
     * @param <T> type of claim
     * @return extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claims_resolver) {
        Claims claims = extractAllClaims(token);
        return claims_resolver.apply(claims);
    }

    /**
     * Extracts all claims from JWT token.
     *
     * @param token the JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret_key_string.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Checks if token is expired.
     *
     * @param token the JWT token
     * @return true if expired
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validates JWT token.
     *
     * @param token the JWT token
     * @return true if valid
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates JWT token for specific user.
     *
     * @param token the JWT token
     * @param username the username to validate against
     * @return true if valid for user
     */
    public boolean isTokenValid(String token, String username) {
        String token_username = extractUsername(token);
        return (token_username.equals(username) && !isTokenExpired(token));
    }
}

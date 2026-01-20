package com.yowyob.feedback.service;

import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for JwtService.
 * Tests JWT token generation, validation, and extraction.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwt_service;

    private AppUser test_user;
    private UUID test_user_id;
    private static final String SECRET_KEY_STRING = "your_very_secret_and_long_key_for_hmac_signature_256_bits";

    @BeforeEach
    void setUp() {
        test_user_id = UUID.randomUUID();
        test_user = AppUser.builder()
                .user_id(test_user_id)
                .user_type(UserType.PERSON)
                .user_firstname("John")
                .user_lastname("Doe")
                .email("john.doe@example.com")
                .contact("+237123456789")
                .password("encoded_password")
                .registration_date_time(OffsetDateTime.now())
                .certified(false)
                .build();
    }

    @Test
    @DisplayName("Should generate valid JWT token for user with email")
    void shouldGenerateTokenForUserWithEmail() {
        // When
        String token = jwt_service.generateToken(test_user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature

        // Verify token contains correct claims
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(test_user.getEmail());
        assertThat(claims.get("role")).isEqualTo(UserType.PERSON.toString());
        assertThat(claims.get("user_id")).isEqualTo(test_user_id.toString());
    }

    @Test
    @DisplayName("Should generate token with contact when email is null")
    void shouldGenerateTokenWithContactWhenEmailIsNull() {
        // Given
        test_user.setEmail(null);

        // When
        String token = jwt_service.generateToken(test_user);

        // Then
        assertThat(token).isNotNull();

        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(test_user.getContact());
    }

    @Test
    @DisplayName("Should generate token with correct expiration time")
    void shouldGenerateTokenWithCorrectExpirationTime() {
        // Given
        // On définit la durée attendue (24 heures en millisecondes)
        long duration24h = 86400000L;
        long currentTime = System.currentTimeMillis();

        // When
        String token = jwt_service.generateToken(test_user);

        // Then
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

        // Extraction des claims
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Date expiration = claims.getExpiration();
        long expectedExpirationTime = currentTime + duration24h;

        // Utilisation de isCloseTo avec une marge de 2 secondes (2000ms)
        // Cela absorbe les délais d'exécution et les arrondis à la seconde inférieure
        assertThat(expiration.getTime())
                .as("The token expiration should be approximately 24 hours from now")
                .isCloseTo(expectedExpirationTime, within(2000L));
    }
    @Test
    @DisplayName("Should validate token and extract user ID successfully")
    void shouldValidateTokenAndExtractUserId() {
        // Given
        String token = jwt_service.generateToken(test_user);

        // When
        Mono<UUID> result = jwt_service.validateTokenAndExtractUserId(token);

        // Then
        StepVerifier.create(result)
                .expectNext(test_user_id)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail validation for invalid token format")
    void shouldFailValidationForInvalidTokenFormat() {
        // Given
        String invalid_token = "invalid.token.format";

        // When
        Mono<UUID> result = jwt_service.validateTokenAndExtractUserId(invalid_token);

        // Then
        StepVerifier.create(result)
                .expectError(MalformedJwtException.class)
                .verify();
    }

    @Test
    @DisplayName("Should fail validation for token with wrong signature")
    void shouldFailValidationForTokenWithWrongSignature() {
        // Given - Create token with different secret
        String wrong_secret = "different_secret_key_for_testing_signature_validation_here";
        SecretKey wrong_key = Keys.hmacShaKeyFor(wrong_secret.getBytes(StandardCharsets.UTF_8));

        String token_with_wrong_signature = Jwts.builder()
                .subject(test_user.getEmail())
                .claim("user_id", test_user_id.toString())
                .signWith(wrong_key)
                .compact();

        // When
        Mono<UUID> result = jwt_service.validateTokenAndExtractUserId(token_with_wrong_signature);

        // Then
        StepVerifier.create(result)
                .expectError(SignatureException.class)
                .verify();
    }

    @Test
    @DisplayName("Should fail validation for expired token")
    void shouldFailValidationForExpiredToken() {
        // Given - Create expired token
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
        long past_time = System.currentTimeMillis() - 1000000; // Long past

        String expired_token = Jwts.builder()
                .subject(test_user.getEmail())
                .claim("user_id", test_user_id.toString())
                .issuedAt(new Date(past_time))
                .expiration(new Date(past_time + 1000)) // Expired 1 second after issue
                .signWith(key)
                .compact();

        // When
        Mono<UUID> result = jwt_service.validateTokenAndExtractUserId(expired_token);

        // Then
        StepVerifier.create(result)
                .expectError(ExpiredJwtException.class)
                .verify();
    }

    @Test
    @DisplayName("Should extract user ID using unsafe method")
    void shouldExtractUserIdUnsafe() {
        // Given
        String token = jwt_service.generateToken(test_user);

        // When
        UUID extracted_user_id = jwt_service.extractUserIdUnsafe(token);

        // Then
        assertThat(extracted_user_id).isEqualTo(test_user_id);
    }

    @Test
    @DisplayName("Should throw exception when extracting from invalid token")
    void shouldThrowExceptionWhenExtractingFromInvalidToken() {
        // Given
        String invalid_token = "invalid.token.format";

        // When & Then
        assertThrows(MalformedJwtException.class, () -> {
            jwt_service.extractUserIdUnsafe(invalid_token);
        });
    }

    @Test
    @DisplayName("Should generate different tokens for same user at different times")
    void shouldGenerateDifferentTokensForSameUser() throws InterruptedException {
        // Given
        String token1 = jwt_service.generateToken(test_user);

        // On attend 1.1 seconde pour être certain que le timestamp 'iat' change
        // car JWT ne gère que les secondes, pas les millisecondes.
        Thread.sleep(1100);

        String token2 = jwt_service.generateToken(test_user);

        // Then
        assertThat(token1)
                .as("Tokens generated at different seconds should be different")
                .isNotEqualTo(token2);

        // But both should contain same user ID
        UUID user_id1 = jwt_service.extractUserIdUnsafe(token1);
        UUID user_id2 = jwt_service.extractUserIdUnsafe(token2);

        assertThat(user_id1).isEqualTo(user_id2).isEqualTo(test_user_id);
    }

    @Test
    @DisplayName("Should include role claim in token")
    void shouldIncludeRoleClaimInToken() {
        // Given
        test_user.setUser_type(UserType.ORGANIZATION);

        // When
        String token = jwt_service.generateToken(test_user);

        // Then
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.get("role")).isEqualTo(UserType.ORGANIZATION.toString());
    }

    @Test
    @DisplayName("Should validate token created immediately")
    void shouldValidateTokenCreatedImmediately() {
        // Given
        String token = jwt_service.generateToken(test_user);

        // When - Validate immediately
        Mono<UUID> result = jwt_service.validateTokenAndExtractUserId(token);

        // Then
        StepVerifier.create(result)
                .assertNext(user_id -> {
                    assertThat(user_id).isEqualTo(test_user_id);
                })
                .verifyComplete();
    }
}

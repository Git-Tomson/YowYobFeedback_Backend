package com.yowyob.feedback.service;

import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtService.
 * Tests JWT token generation, validation, and claim extraction.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    @InjectMocks
    private JwtService jwt_service;

    private AppUser test_user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwt_service, "secret_key_string", "UnePhraseTresLongueDePlusDe32CaracteresPourLaCleSecrete");
        ReflectionTestUtils.setField(jwt_service, "expiration_time", 3600000);
        test_user = AppUser.builder()
                .user_id(UUID.randomUUID())
                .user_type(UserType.PERSON)
                .user_firstname("John")
                .user_lastname("Doe")
                .email("john.doe@example.com")
                .password("encoded_password")
                .build();
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void shouldGenerateValidToken() {
        String token = jwt_service.generateToken(test_user);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsernameFromToken() {
        String token = jwt_service.generateToken(test_user);
        String username = jwt_service.extractUsername(token);

        assertThat(username).isEqualTo(test_user.getEmail());
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateTokenSuccessfully() {
        String token = jwt_service.generateToken(test_user);
        boolean is_valid = jwt_service.isTokenValid(token);

        assertThat(is_valid).isTrue();
    }

    @Test
    @DisplayName("Should validate token for specific username")
    void shouldValidateTokenForUsername() {
        String token = jwt_service.generateToken(test_user);
        boolean is_valid = jwt_service.isTokenValid(token, test_user.getEmail());

        assertThat(is_valid).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid token")
    void shouldRejectInvalidToken() {
        String invalid_token = "invalid.jwt.token";
        boolean is_valid = jwt_service.isTokenValid(invalid_token);

        assertThat(is_valid).isFalse();
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void shouldExtractExpirationDate() {
        String token = jwt_service.generateToken(test_user);
        var expiration = jwt_service.extractExpiration(token);

        assertThat(expiration).isNotNull();
        assertThat(expiration).isAfter(new java.util.Date());
    }
}

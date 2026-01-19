package com.yowyob.feedback.service;

import com.yowyob.feedback.constant.AppConstants;
import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.PasswordResetToken;
import com.yowyob.feedback.entity.UserType;
import com.yowyob.feedback.repository.AppUserRepository;
import com.yowyob.feedback.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PasswordResetService.
 * Tests password reset token generation and validation.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetService Unit Tests")
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository password_reset_token_repository;

    @Mock
    private AppUserRepository app_user_repository;

    @Mock
    private PasswordEncoder password_encoder;

    @InjectMocks
    private PasswordResetService password_reset_service;

    private AppUser test_user;
    private PasswordResetToken test_token;

    @BeforeEach
    void setUp() {
        test_user = AppUser.builder()
                .user_id(UUID.randomUUID())
                .user_type(UserType.PERSON)
                .user_firstname("John")
                .user_lastname("Doe")
                .email("john.doe@example.com")
                .password("encoded_password")
                .build();

        test_token = PasswordResetToken.builder()
                .token_id(UUID.randomUUID())
                .token("reset_token_123")
                .user_id(test_user.getUser_id())
                .expiry_date(OffsetDateTime.now().plusHours(24))
                .used(false)
                .created_at(OffsetDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should initiate password reset successfully")
    void shouldInitiatePasswordResetSuccessfully() {
        when(app_user_repository.findByEmailOrContact(anyString())).thenReturn(Mono.just(test_user));
        when(password_reset_token_repository.deleteExpiredTokensByUserId(any(UUID.class), any(OffsetDateTime.class)))
                .thenReturn(Mono.empty());
        when(password_reset_token_repository.save(any(PasswordResetToken.class))).thenReturn(Mono.just(test_token));

        StepVerifier.create(password_reset_service.initiatePasswordReset("john.doe@example.com"))
                .assertNext(message -> {
                    assertThat(message).isEqualTo(AppConstants.PASSWORD_RESET_EMAIL_SENT);
                })
                .verifyComplete();

        verify(app_user_repository, times(1)).findByEmailOrContact(anyString());
        verify(password_reset_token_repository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("Should fail password reset when user not found")
    void shouldFailPasswordResetWhenUserNotFound() {
        when(app_user_repository.findByEmailOrContact(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(password_reset_service.initiatePasswordReset("nonexistent@example.com"))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.USER_NOT_FOUND_MESSAGE)
                )
                .verify();

        verify(password_reset_token_repository, never()).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("Should confirm password reset successfully")
    void shouldConfirmPasswordResetSuccessfully() {
        when(password_reset_token_repository.findValidToken(anyString(), any(OffsetDateTime.class)))
                .thenReturn(Mono.just(test_token));
        when(app_user_repository.findById(any(UUID.class))).thenReturn(Mono.just(test_user));
        when(password_encoder.encode(anyString())).thenReturn("new_encoded_password");
        when(app_user_repository.save(any(AppUser.class))).thenReturn(Mono.just(test_user));
        when(password_reset_token_repository.save(any(PasswordResetToken.class))).thenReturn(Mono.just(test_token));

        StepVerifier.create(password_reset_service.confirmPasswordReset("reset_token_123", "newPassword123"))
                .assertNext(message -> {
                    assertThat(message).isEqualTo(AppConstants.PASSWORD_RESET_SUCCESS);
                })
                .verifyComplete();

        verify(app_user_repository, times(1)).save(any(AppUser.class));
        verify(password_reset_token_repository, times(1)).save(any(PasswordResetToken.class));
    }

    @Test
    @DisplayName("Should fail password reset with invalid token")
    void shouldFailPasswordResetWithInvalidToken() {
        when(password_reset_token_repository.findValidToken(anyString(), any(OffsetDateTime.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(password_reset_service.confirmPasswordReset("invalid_token", "newPassword123"))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.INVALID_OR_EXPIRED_TOKEN)
                )
                .verify();

        verify(app_user_repository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should fail password reset with already used token")
    void shouldFailPasswordResetWithUsedToken() {
        test_token.setUsed(true);
        when(password_reset_token_repository.findValidToken(anyString(), any(OffsetDateTime.class)))
                .thenReturn(Mono.just(test_token));

        StepVerifier.create(password_reset_service.confirmPasswordReset("reset_token_123", "newPassword123"))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.PASSWORD_RESET_TOKEN_ALREADY_USED)
                )
                .verify();

        verify(app_user_repository, never()).save(any(AppUser.class));
    }
}

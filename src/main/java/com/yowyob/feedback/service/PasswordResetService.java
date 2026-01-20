package com.yowyob.feedback.service;

import com.yowyob.feedback.constant.AppConstants;
import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.PasswordResetToken;
import com.yowyob.feedback.repository.AppUserRepository;
import com.yowyob.feedback.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service for password reset operations.
 * Handles token generation, validation, and password update.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository password_reset_token_repository;
    private final AppUserRepository app_user_repository;
    private final PasswordEncoder password_encoder;
    // TODO: Inject EmailService when implemented

    /**
     * Initiates password reset by generating token and sending email.
     *
     * @param email user's email address
     * @return Mono<String> success message
     */
    @Transactional
    public Mono<String> initiatePasswordReset(String email) {
        log.info("Password reset requested for email: {}", email);

        return app_user_repository.findByEmailOrContact(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(AppConstants.USER_NOT_FOUND_MESSAGE)))
                .flatMap(user -> cleanupOldTokens(user.getUser_id())
                        .then(createResetToken(user)))
                .flatMap(token -> sendResetEmail(token)
                        .thenReturn(AppConstants.PASSWORD_RESET_EMAIL_SENT))
                .doOnSuccess(msg -> log.info("Password reset email sent successfully"))
                .doOnError(error -> log.error("Password reset failed: {}", error.getMessage()));
    }

    /**
     * Confirms password reset using token and sets new password.
     *
     * @param token reset token
     * @param new_password new password
     * @return Mono<String> success message
     */
    @Transactional
    public Mono<String> confirmPasswordReset(String token, String new_password) {
        log.info("Password reset confirmation attempt");

        OffsetDateTime current_time = OffsetDateTime.now();

        return password_reset_token_repository.findValidToken(token, current_time)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(AppConstants.INVALID_OR_EXPIRED_TOKEN)))
                .flatMap(reset_token -> {
                    if (Boolean.TRUE.equals(reset_token.getUsed())) {
                        return Mono.error(new IllegalArgumentException(
                                AppConstants.PASSWORD_RESET_TOKEN_ALREADY_USED));
                    }
                    return updatePassword(reset_token, new_password);
                })
                .doOnSuccess(msg -> log.info("Password reset successful"))
                .doOnError(error -> log.error("Password reset confirmation failed: {}", error.getMessage()));
    }

    /**
     * Cleans up expired tokens for a user.
     *
     * @param user_id user ID
     * @return Mono<Void>
     */
    private Mono<Void> cleanupOldTokens(UUID user_id) {
        OffsetDateTime current_time = OffsetDateTime.now();
        return password_reset_token_repository.deleteExpiredTokensByUserId(user_id, current_time);
    }

    /**
     * Creates a new password reset token.
     *
     * @param user the user
     * @return Mono<PasswordResetToken>
     */
    private Mono<PasswordResetToken> createResetToken(AppUser user) {
        String token = UUID.randomUUID().toString();
        OffsetDateTime expiry_date = OffsetDateTime.now()
                .plusHours(AppConstants.PASSWORD_RESET_TOKEN_EXPIRATION_HOURS);

        PasswordResetToken reset_token = PasswordResetToken.builder()
                .token(token)
                .user_id(user.getUser_id())
                .expiry_date(expiry_date)
                .used(false)
                .created_at(OffsetDateTime.now())
                .build();

        return password_reset_token_repository.save(reset_token);
    }

    /**
     * Sends password reset email to user.
     *
     * @param token the reset token
     * @return Mono<Void>
     */
    private Mono<Void> sendResetEmail(PasswordResetToken token) {
        // TODO: Implement email sending when EmailService is ready
        log.info("Password reset token generated: {}", token.getToken());
        log.info("This token should be sent via email in production");
        return Mono.empty();
    }

    /**
     * Updates user password and marks token as used.
     *
     * @param reset_token the reset token
     * @param new_password new password
     * @return Mono<String> success message
     */
    private Mono<String> updatePassword(PasswordResetToken reset_token, String new_password) {
        return app_user_repository.findById(reset_token.getUser_id())
                .flatMap(user -> {
                    user.setPassword(password_encoder.encode(new_password));
                    return app_user_repository.save(user);
                })
                .then(markTokenAsUsed(reset_token))
                .thenReturn(AppConstants.PASSWORD_RESET_SUCCESS);
    }

    /**
     * Marks reset token as used.
     *
     * @param token the token to mark
     * @return Mono<PasswordResetToken>
     */
    private Mono<PasswordResetToken> markTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        return password_reset_token_repository.save(token);
    }
}

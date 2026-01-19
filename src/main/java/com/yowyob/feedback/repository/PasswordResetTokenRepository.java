package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.PasswordResetToken;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Repository interface for PasswordResetToken entity.
 * Provides reactive database operations for password reset functionality.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@Repository
public interface PasswordResetTokenRepository extends ReactiveCrudRepository<PasswordResetToken, UUID> {

    /**
     * Finds a valid (unused and not expired) password reset token.
     *
     * @param token the token string
     * @param current_time current timestamp for expiry check
     * @return Mono containing the token if found and valid
     */
    @Query("SELECT * FROM password_reset_token WHERE token = :token AND used = false AND expiry_date > :current_time")
    Mono<PasswordResetToken> findValidToken(String token, OffsetDateTime current_time);

    /**
     * Finds a password reset token by token string.
     *
     * @param token the token string
     * @return Mono containing the token if found
     */
    Mono<PasswordResetToken> findByToken(String token);

    /**
     * Deletes all expired tokens for a specific user.
     *
     * @param user_id the user ID
     * @param current_time current timestamp
     * @return Mono<Void>
     */
    @Query("DELETE FROM password_reset_token WHERE user_id = :user_id AND expiry_date < :current_time")
    Mono<Void> deleteExpiredTokensByUserId(UUID user_id, OffsetDateTime current_time);
}

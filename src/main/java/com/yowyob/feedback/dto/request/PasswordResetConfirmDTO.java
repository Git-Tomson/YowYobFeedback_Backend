package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import static com.yowyob.feedback.constant.AppConstants.MIN_PASSWORD_LENGTH;
import static com.yowyob.feedback.constant.AppConstants.MIN_PASSWORD_LENGTH_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.PASSWORD_REQUIRED_MESSAGE;

/**
 * DTO for confirming password reset with new password.
 * Used when user sets a new password using reset token.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@Builder
public record PasswordResetConfirmDTO(
        @NotBlank(message = "Token is required")
        String token,

        @NotBlank(message = PASSWORD_REQUIRED_MESSAGE)
        @Size(min = MIN_PASSWORD_LENGTH, message = MIN_PASSWORD_LENGTH_MESSAGE)
        String new_password
) {}

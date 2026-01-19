package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import static com.yowyob.feedback.constant.AppConstants.EMAIL_VALID_MESSAGE;

/**
 * DTO for password reset request.
 * Used when user requests to reset their password.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@Builder
public record PasswordResetRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = EMAIL_VALID_MESSAGE)
        String email
) {}

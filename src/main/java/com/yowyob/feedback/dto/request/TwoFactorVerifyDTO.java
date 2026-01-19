package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * DTO for 2FA verification.
 * Used when user provides 2FA code during login.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@Builder
public record TwoFactorVerifyDTO(
        @NotBlank(message = "Identifier is required")
        String identifier,

        @NotBlank(message = "2FA code is required")
        String code
) {}

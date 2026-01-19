package com.yowyob.feedback.dto.response;

import lombok.Builder;

/**
 * DTO for 2FA setup response.
 * Contains QR code and backup codes for user to save.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@Builder
public record TwoFactorSetupResponseDTO(
        String secret,
        String qr_code_url,
        String[] backup_codes
) {}

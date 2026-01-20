package com.yowyob.feedback.service;

import dev.samstevens.totp.exceptions.QrGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TwoFactorService.
 * Tests TOTP generation, verification, and backup code management.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TwoFactorService Unit Tests")
class TwoFactorServiceTest {

    @InjectMocks
    private TwoFactorService two_factor_service;

    private String test_secret;

    @BeforeEach
    void setUp() {
        test_secret = two_factor_service.generateSecret();
    }

    @Test
    @DisplayName("Should generate secret successfully")
    void shouldGenerateSecret() {
        String secret = two_factor_service.generateSecret();

        assertThat(secret).isNotNull();
        assertThat(secret).isNotEmpty();
        assertThat(secret.length()).isGreaterThan(10);
    }

    @Test
    @DisplayName("Should generate QR code URL successfully")
    void shouldGenerateQrCodeUrl() throws QrGenerationException {
        String qr_url = two_factor_service.generateQrCodeUrl(test_secret, "test@example.com");

        assertThat(qr_url).isNotNull();
        assertThat(qr_url).startsWith("data:image/png;base64,");
    }

    @Test
    @DisplayName("Should generate backup codes")
    void shouldGenerateBackupCodes() {
        String[] backup_codes = two_factor_service.generateBackupCodes();

        assertThat(backup_codes).isNotNull();
        assertThat(backup_codes).hasSize(8);
        assertThat(backup_codes[0]).hasSize(8);
        assertThat(backup_codes[0]).matches("[A-Z0-9]+");
    }

    @Test
    @DisplayName("Should verify valid backup code")
    void shouldVerifyValidBackupCode() {
        String[] backup_codes = new String[]{"ABCD1234", "EFGH5678", "IJKL9012"};
        boolean is_valid = two_factor_service.verifyBackupCode(backup_codes, "ABCD1234");

        assertThat(is_valid).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid backup code")
    void shouldRejectInvalidBackupCode() {
        String[] backup_codes = new String[]{"ABCD1234", "EFGH5678", "IJKL9012"};
        boolean is_valid = two_factor_service.verifyBackupCode(backup_codes, "INVALID");

        assertThat(is_valid).isFalse();
    }

    @Test
    @DisplayName("Should remove used backup code")
    void shouldRemoveUsedBackupCode() {
        String[] backup_codes = new String[]{"ABCD1234", "EFGH5678", "IJKL9012"};
        String[] updated_codes = two_factor_service.removeBackupCode(backup_codes, "ABCD1234");

        assertThat(updated_codes).hasSize(2);
        assertThat(updated_codes).doesNotContain("ABCD1234");
        assertThat(updated_codes).contains("EFGH5678", "IJKL9012");
    }

    @Test
    @DisplayName("Should handle null backup codes")
    void shouldHandleNullBackupCodes() {
        boolean is_valid = two_factor_service.verifyBackupCode(null, "ABCD1234");

        assertThat(is_valid).isFalse();
    }

    @Test
    @DisplayName("Should handle null provided code")
    void shouldHandleNullProvidedCode() {
        String[] backup_codes = new String[]{"ABCD1234"};
        boolean is_valid = two_factor_service.verifyBackupCode(backup_codes, null);

        assertThat(is_valid).isFalse();
    }
}

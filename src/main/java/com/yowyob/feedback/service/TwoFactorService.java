package com.yowyob.feedback.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Service for Two-Factor Authentication operations.
 * Handles TOTP generation, verification, and QR code creation.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorService {

    private static final String ISSUER = "YowyobFeedback";
    private static final int BACKUP_CODES_COUNT = 8;
    private static final int BACKUP_CODE_LENGTH = 8;
    private static final String BACKUP_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    /**
     * Generates a new secret for TOTP authentication.
     *
     * @return the generated secret
     */
    public String generateSecret() {
        SecretGenerator secret_generator = new DefaultSecretGenerator();
        return secret_generator.generate();
    }

    /**
     * Generates a QR code URL for the given secret and user email.
     *
     * @param secret the TOTP secret
     * @param email the user's email
     * @return QR code data URL
     * @throws QrGenerationException if QR generation fails
     */
    public String generateQrCodeUrl(String secret, String email) throws QrGenerationException {
        QrData qr_data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator qr_generator = new ZxingPngQrGenerator();
        byte[] image_data = qr_generator.generate(qr_data);

        return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(image_data);
    }

    /**
     * Verifies a TOTP code against the secret.
     *
     * @param secret the TOTP secret
     * @param code the code to verify
     * @return true if code is valid
     */
    public boolean verifyCode(String secret, String code) {
        TimeProvider time_provider = new SystemTimeProvider();
        CodeGenerator code_generator = new DefaultCodeGenerator();
        CodeVerifier code_verifier = new DefaultCodeVerifier(code_generator, time_provider);

        return code_verifier.isValidCode(secret, code);
    }

    /**
     * Generates backup codes for 2FA recovery.
     *
     * @return array of backup codes
     */
    public String[] generateBackupCodes() {
        SecureRandom random = new SecureRandom();
        String[] backup_codes = new String[BACKUP_CODES_COUNT];

        for (int i = 0; i < BACKUP_CODES_COUNT; i++) {
            StringBuilder code = new StringBuilder(BACKUP_CODE_LENGTH);
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                code.append(BACKUP_CODE_CHARS.charAt(random.nextInt(BACKUP_CODE_CHARS.length())));
            }
            backup_codes[i] = code.toString();
        }

        return backup_codes;
    }

    /**
     * Verifies if a backup code is valid.
     *
     * @param backup_codes array of valid backup codes
     * @param provided_code the code to verify
     * @return true if code is found in backup codes
     */
    public boolean verifyBackupCode(String[] backup_codes, String provided_code) {
        if (backup_codes == null || provided_code == null) {
            return false;
        }
        return Arrays.asList(backup_codes).contains(provided_code.toUpperCase());
    }

    /**
     * Removes a used backup code from the array.
     *
     * @param backup_codes current backup codes
     * @param used_code the code that was used
     * @return new array without the used code
     */
    public String[] removeBackupCode(String[] backup_codes, String used_code) {
        return Arrays.stream(backup_codes)
                .filter(code -> !code.equals(used_code.toUpperCase()))
                .toArray(String[]::new);
    }
}

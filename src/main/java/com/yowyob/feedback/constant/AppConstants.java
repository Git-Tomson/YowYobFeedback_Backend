package com.yowyob.feedback.constant;

/**
 * Application-wide constants.
 * Contains all constant values used across the application.
 *
 * @author Thomas Djotio Ndié
 * @version 1.0
 * @since 2024-12-12
 */
public final class AppConstants {

    private AppConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final int MIN_PASSWORD_LENGTH = 8;

    public static final String MIN_PASSWORD_LENGTH_MESSAGE = "Password must be at least 8 characters long";

    public static final String PASSWORD_REQUIRED_MESSAGE = "Password is required";

    public static final String EMAIL_VALID_MESSAGE = "Email must be valid";

    public static final String USER_TYPE_REQUIRED_MESSAGE = "User type is required";

    public static final String USER_LASTNAME_REQUIRED_MESSAGE = "Last name is required";

    public static final String REGISTRATION_SUCCESS_MESSAGE = "Registration successful";

    public static final String LOGIN_SUCCESS_MESSAGE = "Login successful";

    public static final String USER_NOT_FOUND_MESSAGE = "User not found";

    public static final String INVALID_PASSWORD_MESSAGE = "Invalid password";

    public static final String USER_ALREADY_EXISTS_MESSAGE = "User with this email or contact already exists";

    public static final String EMAIL_OR_CONTACT_REQUIRED_MESSAGE = "Either email or contact must be provided";

    public static final String OCCUPATION_REQUIRED_FOR_PERSON_MESSAGE = "Occupation is required for person type";

    public static final String LOCATION_REQUIRED_FOR_ORGANIZATION_MESSAGE = "Location is required for organization type";

    // Password reset messages
    public static final String PASSWORD_RESET_EMAIL_SENT = "Password reset email sent successfully";
    public static final String PASSWORD_RESET_SUCCESS = "Password reset successful";
    public static final String INVALID_OR_EXPIRED_TOKEN = "Invalid or expired token";
    public static final String PASSWORD_RESET_TOKEN_ALREADY_USED = "This reset token has already been used";

    // 2FA messages
    public static final String TWO_FA_ENABLED_SUCCESS = "Two-factor authentication enabled successfully";
    public static final String TWO_FA_DISABLED_SUCCESS = "Two-factor authentication disabled successfully";
    public static final String INVALID_TWO_FA_CODE = "Invalid 2FA code";
    public static final String TWO_FA_REQUIRED = "Two-factor authentication required";
    public static final String TWO_FA_NOT_ENABLED = "Two-factor authentication is not enabled";

    // Token expiration
    public static final long PASSWORD_RESET_TOKEN_EXPIRATION_HOURS = 24;

}

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

    public static final String TOKEN_SIGNATURE = "YF_";
}

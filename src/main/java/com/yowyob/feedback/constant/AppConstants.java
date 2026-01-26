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

    // Project validation messages
    public static final String PROJECT_NAME_REQUIRED_MESSAGE = "Project name is required";
    public static final String PROJECT_NAME_TOO_LONG_MESSAGE = "Project name cannot exceed 255 characters";
    public static final String PROJECT_DESCRIPTION_TOO_LONG_MESSAGE = "Project description is too long";
    public static final String PROJECT_CODE_REQUIRED_MESSAGE = "Project code is required";
    public static final String PROJECT_CODE_INVALID_LENGTH_MESSAGE = "Project code must be exactly 6 characters";
    public static final String PROJECT_ID_REQUIRED_MESSAGE = "Project ID is required";

    // Member validation messages
    public static final String MEMBER_PSEUDO_REQUIRED_MESSAGE = "Member pseudo is required";
    public static final String MEMBER_PSEUDO_TOO_LONG_MESSAGE = "Member pseudo cannot exceed 50 characters";

    // Project business messages
    public static final String PROJECT_CREATED_SUCCESS_MESSAGE = "Project created successfully";
    public static final String PROJECT_UPDATED_SUCCESS_MESSAGE = "Project updated successfully";
    public static final String PROJECT_DELETED_SUCCESS_MESSAGE = "Project deleted successfully";
    public static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found";
    public static final String PROJECT_NAME_ALREADY_EXISTS_MESSAGE = "A project with this name already exists for this user";
    public static final String PROJECT_CODE_INVALID_MESSAGE = "Invalid project code";
    public static final String PROJECT_UNAUTHORIZED_MESSAGE = "You are not authorized to perform this action on this project";

    // Member business messages
    public static final String MEMBER_JOINED_SUCCESS_MESSAGE = "Successfully joined the project";
    public static final String MEMBER_REMOVED_SUCCESS_MESSAGE = "Member removed successfully";
    public static final String MEMBER_LEFT_SUCCESS_MESSAGE = "You have left the project";
    public static final String MEMBER_NOT_FOUND_MESSAGE = "Member not found";
    public static final String MEMBER_ALREADY_EXISTS_MESSAGE = "You are already a member of this project";
    public static final String MEMBER_PSEUDO_ALREADY_EXISTS_MESSAGE = "This pseudo is already used in this project";
    public static final String CANNOT_REMOVE_CREATOR_MESSAGE = "Cannot remove the project creator";
    public static final String CANNOT_LEAVE_YOUR_OWN_PROJECT_MESSAGE = "You cannot leave your own project";

    // Project constants
    public static final int PROJECT_CODE_LENGTH = 6;
    public static final int PROJECT_NAME_MAX_LENGTH = 255;
    public static final int PROJECT_DESCRIPTION_MAX_LENGTH = 2000;
    public static final int MEMBER_PSEUDO_MAX_LENGTH = 50;

    public static final String FORBIDDEN_DELETE_FEEDBACK_MESSAGE = "You are not authorized to delete this feedback";
    public static final String FORBIDDEN_UPDATE_FEEDBACK_MESSAGE = "You are not authorized to update this feedback";
    public static final String FORBIDDEN_DELETE_COMMENT_MESSAGE = "You are not authorized to delete this comment";
    public static final String FORBIDDEN_UPDATE_COMMENT_MESSAGE = "You are not authorized to update this comment";

    // Profile update messages
    public static final String PROFILE_UPDATE_SUCCESS_MESSAGE = "Profile updated successfully";
    public static final String EMAIL_ALREADY_USED_MESSAGE = "Email is already used by another user";
    public static final String CONTACT_ALREADY_USED_MESSAGE = "Contact is already used by another user";
    public static final String CANNOT_REMOVE_BOTH_EMAIL_AND_CONTACT_MESSAGE = "Cannot remove both email and contact";

    // JWT messages
    public static final String INVALID_TOKEN_MESSAGE = "Invalid or expired token";
    public static final String MISSING_TOKEN_MESSAGE = "Authorization token is missing";

    // Profile error messages
    public static final String PERSON_DATA_NOT_FOUND_MESSAGE = "Person data not found for this user";
    public static final String ORGANIZATION_DATA_NOT_FOUND_MESSAGE = "Organization data not found for this user";

}

package com.yowyob.feedback.dto.request;

import com.yowyob.feedback.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import static com.yowyob.feedback.constant.AppConstants.*;

/**
 * Data Transfer Object for profile update request.
 * All fields are optional - only provided fields will be updated.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-24
 * @version 1.0
 */
@Builder
public record UpdateProfileRequestDTO(
        String user_firstname,
        String user_lastname,

        @Email(message = EMAIL_VALID_MESSAGE)
        String email,

        String contact,
        String user_logo,
        String domain,
        String description,

        @Size(min = MIN_PASSWORD_LENGTH, message = MIN_PASSWORD_LENGTH_MESSAGE)
        String password,

        String occupation,
        String location
) {}

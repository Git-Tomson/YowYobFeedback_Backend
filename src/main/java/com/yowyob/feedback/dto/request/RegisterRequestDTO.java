/*
package com.yowyob.feedback.dto.request;

import com.yowyob.feedback.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static com.yowyob.feedback.constant.AppConstants.EMAIL_VALID_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.PASSWORD_REQUIRED_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.MIN_PASSWORD_LENGTH;
import static com.yowyob.feedback.constant.AppConstants.MIN_PASSWORD_LENGTH_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.USER_TYPE_REQUIRED_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.USER_LASTNAME_REQUIRED_MESSAGE;


/**
 * Data Transfer Object for user registration request.
 * Contains all the necessary information to create a new user account.
 *
 * Validation rules:
 * - user_type is mandatory (PERSON or ORGANIZATION)
 * - user_lastname is mandatory
 * - password must be at least 8 characters
 * - email must be valid if provided
 * - at least one of email or contact must be provided (validated in service layer)
 * - occupation is required for PERSON type
 * - location is required for ORGANIZATION type
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 1.0

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterRequestDTO {

    @NotNull(message = USER_TYPE_REQUIRED_MESSAGE)
    private UserType user_type;

    private String user_firstname;

    @NotBlank(message = USER_LASTNAME_REQUIRED_MESSAGE)
    private String user_lastname;

    @Email(message = EMAIL_VALID_MESSAGE)
    private String email;

    @NotBlank(message = PASSWORD_REQUIRED_MESSAGE)
    @Size(min = MIN_PASSWORD_LENGTH, message = MIN_PASSWORD_LENGTH_MESSAGE)
    private String password;

    private String contact;

    private String user_logo;

    private String domain;

    private String description;

    private String occupation;

    private String location;
}
*/
package com.yowyob.feedback.dto.request;

import com.yowyob.feedback.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import static com.yowyob.feedback.constant.AppConstants.USER_LASTNAME_REQUIRED_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.USER_TYPE_REQUIRED_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.EMAIL_VALID_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.PASSWORD_REQUIRED_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.MIN_PASSWORD_LENGTH;
import static com.yowyob.feedback.constant.AppConstants.MIN_PASSWORD_LENGTH_MESSAGE;


/**
 * Data Transfer Object for user registration request (Record version).
 *
 * @author Thomas Djotio Ndié
 * @since 2025-12-19
 * @version 2.0
 */
@Builder
public record RegisterRequestDTO(

        @NotNull(message = USER_TYPE_REQUIRED_MESSAGE)
        UserType user_type,

        String user_firstname,

        @NotBlank(message = USER_LASTNAME_REQUIRED_MESSAGE)
        String user_lastname,

        @Email(message = EMAIL_VALID_MESSAGE)
        String email,

        @NotBlank(message = PASSWORD_REQUIRED_MESSAGE)
        @Size(min = MIN_PASSWORD_LENGTH, message = MIN_PASSWORD_LENGTH_MESSAGE)
        String password,

        String contact,

        String user_logo,

        String domain,

        String description,

        String occupation,

        String location
) {}


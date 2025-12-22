/*
package com.yowyob.feedback.dto.response;

import com.yowyob.feedback.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for user response.
 * This DTO contains user information that can be safely exposed to clients.
 * Sensitive information like user_id and password are excluded.
 *
 * Includes both common user attributes and type-specific attributes
 * (occupation for persons, location for organizations).
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
public class UserResponseDTO {

    private UserType user_type;

    private String user_firstname;

    private String user_lastname;

    private String email;

    private String contact;

    private String user_logo;

    private String domain;

    private String description;

    private OffsetDateTime registration_date_time;

    private Boolean certified;

    private String occupation;

    private String location;
}
 */
package com.yowyob.feedback.dto.response;

import com.yowyob.feedback.entity.UserType;
import lombok.Builder;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for user response (Record version).
 *
 * @author Thomas Djotio Ndié
 * @since 2025-12-19
 * @version 2.0
 */
@Builder(toBuilder = true)
public record UserResponseDTO(
        UserType user_type,
        String user_firstname,
        String user_lastname,
        String email,
        String contact,
        String user_logo,
        String domain,
        String description,
        OffsetDateTime registration_date_time,
        Boolean certified,
        String occupation,
        String location
) {}

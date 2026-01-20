package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import static com.yowyob.feedback.constant.AppConstants.*;

/**
 * Data Transfer Object for project creation request.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Builder
public record CreateProjectRequestDTO(

        @NotBlank(message = PROJECT_NAME_REQUIRED_MESSAGE)
        @Size(max = PROJECT_NAME_MAX_LENGTH, message = PROJECT_NAME_TOO_LONG_MESSAGE)
        String project_name,

        @Size(max = PROJECT_DESCRIPTION_MAX_LENGTH, message = PROJECT_DESCRIPTION_TOO_LONG_MESSAGE)
        String description,

        String project_logo
) {}

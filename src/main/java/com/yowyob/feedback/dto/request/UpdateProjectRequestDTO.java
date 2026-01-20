package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

import static com.yowyob.feedback.constant.AppConstants.*;

/**
 * Data Transfer Object for project update request.
 * All fields are optional - only provided fields will be updated.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Builder
public record UpdateProjectRequestDTO(

        @Size(max = PROJECT_NAME_MAX_LENGTH, message = PROJECT_NAME_TOO_LONG_MESSAGE)
        String project_name,

        @Size(min = PROJECT_CODE_LENGTH, max = PROJECT_CODE_LENGTH,
                message = PROJECT_CODE_INVALID_LENGTH_MESSAGE)
        String code,

        @Size(max = PROJECT_DESCRIPTION_MAX_LENGTH, message = PROJECT_DESCRIPTION_TOO_LONG_MESSAGE)
        String description,

        String project_logo
) {}

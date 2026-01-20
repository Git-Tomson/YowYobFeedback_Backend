package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

import static com.yowyob.feedback.constant.AppConstants.*;

/**
 * Data Transfer Object for joining a project request.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Builder
public record JoinProjectRequestDTO(

        @NotNull(message = PROJECT_ID_REQUIRED_MESSAGE)
        UUID project_id,

        @NotBlank(message = PROJECT_CODE_REQUIRED_MESSAGE)
        @Size(min = PROJECT_CODE_LENGTH, max = PROJECT_CODE_LENGTH,
                message = PROJECT_CODE_INVALID_LENGTH_MESSAGE)
        String code,

        @NotBlank(message = MEMBER_PSEUDO_REQUIRED_MESSAGE)
        @Size(max = MEMBER_PSEUDO_MAX_LENGTH, message = MEMBER_PSEUDO_TOO_LONG_MESSAGE)
        String member_pseudo
) {}

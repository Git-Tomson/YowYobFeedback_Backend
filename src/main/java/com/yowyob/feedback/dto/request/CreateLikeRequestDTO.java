package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for creating a like on a feedback.
 * The liker_id is extracted from the JWT token.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Builder
public record CreateLikeRequestDTO(

        @NotNull(message = "Feedback ID is required")
        UUID feedback_id
) {}

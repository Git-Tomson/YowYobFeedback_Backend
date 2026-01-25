package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for creating a new comment.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Builder
public record CreateCommentRequestDTO(

        @NotNull(message = "Feedback ID is required")
        UUID feedback_id,

        @NotNull(message = "Commenter ID is required")
        UUID commenter_id,

        @NotBlank(message = "Content is required")
        String content
) {}

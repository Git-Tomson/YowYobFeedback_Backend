package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * DTO for updating an existing comment.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Builder
public record UpdateCommentRequestDTO(

        @NotBlank(message = "Content is required")
        String content
) {}

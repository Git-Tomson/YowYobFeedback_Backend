package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new feedback.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Builder
public record CreateFeedbackRequestDTO(

        @NotNull(message = "Project ID is required")
        UUID project_id,

        @NotBlank(message = "Member pseudo is required")
        String member_pseudo,

        @NotBlank(message = "Content is required")
        String content,

        List<String> attachments
) {}

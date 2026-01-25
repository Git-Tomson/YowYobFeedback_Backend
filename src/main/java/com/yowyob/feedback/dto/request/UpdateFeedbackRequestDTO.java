package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

/**
 * DTO for updating an existing feedback.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Builder
public record UpdateFeedbackRequestDTO(

        @NotBlank(message = "Content is required")
        String content,

        List<String> attachments
) {}

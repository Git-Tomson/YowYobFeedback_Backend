package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

/**
 * DTO for creating an approval on a comment.
 * The approver_id is extracted from the JWT token.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Builder
public record CreateApprovalRequestDTO(

        @NotNull(message = "Comment ID is required")
        UUID comments_id
) {}

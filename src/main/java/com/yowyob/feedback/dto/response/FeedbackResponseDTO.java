package com.yowyob.feedback.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for feedback response.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FeedbackResponseDTO(
        UUID feedback_id,
        OffsetDateTime feedback_date_time,
        String content,
        List<String> attachments,
        UUID target_project_id,
        String project_name,
        UUID member_id,
        String member_pseudo,
        Integer number_of_likes,
        Integer number_of_comments
) {}

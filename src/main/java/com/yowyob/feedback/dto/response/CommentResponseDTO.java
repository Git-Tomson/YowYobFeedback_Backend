package com.yowyob.feedback.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for comment response.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CommentResponseDTO(
        UUID comments_id,
        UUID feedback_id,
        UUID commenter_id,
        String commenter_name,
        String content,
        OffsetDateTime comments_date_time,
        Integer number_of_likes
) {}

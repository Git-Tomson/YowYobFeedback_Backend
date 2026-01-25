package com.yowyob.feedback.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for like response.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LikeResponseDTO(
        UUID feedback_id,
        UUID liker_id,
        String liker_name,
        OffsetDateTime likes_date_time
) {}

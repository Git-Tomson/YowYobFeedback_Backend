package com.yowyob.feedback.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for project response.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ProjectResponseDTO(
        UUID project_id,
        String project_name,
        String code,
        String description,
        String project_logo,
        OffsetDateTime creation_date_time,
        Integer number_of_members,
        UUID creator_id
) {}

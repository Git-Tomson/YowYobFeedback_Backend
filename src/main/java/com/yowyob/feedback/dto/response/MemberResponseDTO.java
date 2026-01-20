package com.yowyob.feedback.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.util.UUID;

/**
 * Data Transfer Object for member response.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MemberResponseDTO(
        UUID member_id,
        String member_pseudo,
        UUID user_id,
        UUID project_id,
        String user_firstname,
        String user_lastname,
        String user_logo
) {}

/*
package com.yowyob.feedback.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthResponseDTO {

    private String message;
    private UserResponseDTO user_response_dto;
    private String token; // Pour JWT si on l'implémente plus tard
}
*/
package com.yowyob.feedback.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

/**
 * Data Transfer Object pour la réponse d'authentification.
 */
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
//provide to output JSON the capacity to use underscores without sacrifying java conventions
public record AuthResponseDTO(
        String message,
        UserResponseDTO user_response_dto,
        String token // Pour JWT
) {}

/*
package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import static com.yowyob.feedback.constant.AppConstants.EMAIL_OR_CONTACT_REQUIRED_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.PASSWORD_REQUIRED_MESSAGE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginRequestDTO {

    @NotBlank(message = EMAIL_OR_CONTACT_REQUIRED_MESSAGE)
    private String identifier; // Peut être email ou contact

    @NotBlank(message = PASSWORD_REQUIRED_MESSAGE)
    private String password;
}
 */

package com.yowyob.feedback.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import static com.yowyob.feedback.constant.AppConstants.EMAIL_OR_CONTACT_REQUIRED_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.PASSWORD_REQUIRED_MESSAGE;

/**
 * Version Record du DTO de connexion.
 * Les annotations de validation Jakarta restent sur les composants du record.
 */
@Builder
public record LoginRequestDTO(

        @NotBlank(message = EMAIL_OR_CONTACT_REQUIRED_MESSAGE)
        String identifier,

        @NotBlank(message = PASSWORD_REQUIRED_MESSAGE)
        String password
) {}

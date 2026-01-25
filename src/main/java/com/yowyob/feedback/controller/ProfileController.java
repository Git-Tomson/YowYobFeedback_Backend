package com.yowyob.feedback.controller;

import com.yowyob.feedback.constant.AppConstants;
import com.yowyob.feedback.dto.request.UpdateProfileRequestDTO;
import com.yowyob.feedback.dto.response.AuthResponseDTO;
import com.yowyob.feedback.service.JwtService;
import com.yowyob.feedback.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for user profile management.
 * Handles profile update operations for authenticated users.
 *
 * Base path: /api/v1/profile
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-24
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management API")
public class ProfileController {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    private final ProfileService profile_service;
    private final JwtService jwt_service;

    /**
     * Updates authenticated user profile information.
     * User identity is extracted from JWT token in Authorization header.
     * Only provided fields will be updated.
     *
     * @param update_request the profile update data
     * @param request the HTTP request containing authorization header
     * @return Mono<AuthResponseDTO> containing updated user information
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update authenticated user profile",
            description = "Updates the profile of the currently authenticated user. " +
                    "User is identified via JWT token in Authorization header. " +
                    "Only provided fields will be updated. " +
                    "Email and contact must remain unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data, email/contact already used, " +
                            "or attempting to remove both email and contact"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authorization token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<AuthResponseDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO update_request,
            ServerHttpRequest request) {
        log.info("PATCH /api/v1/profile - Profile update request");

        return extractTokenFromRequest(request)
                .flatMap(jwt_service::validateTokenAndExtractUserId)
                .flatMap(user_id -> profile_service.updateProfile(user_id, update_request))
                .doOnNext(response -> log.info("Profile updated successfully"))
                .doOnTerminate(() -> log.info("Profile update request completed"));
    }

    /**
     * Extracts JWT token from Authorization header.
     *
     * @param request the HTTP request
     * @return Mono<String> containing the JWT token
     */
    private Mono<String> extractTokenFromRequest(ServerHttpRequest request) {
        String auth_header = request.getHeaders().getFirst(AUTHORIZATION_HEADER);

        if (auth_header == null || !auth_header.startsWith(BEARER_PREFIX)) {
            return Mono.error(new IllegalArgumentException(
                    AppConstants.MISSING_TOKEN_MESSAGE));
        }

        String token = auth_header.substring(BEARER_PREFIX_LENGTH);
        return Mono.just(token);
    }
}

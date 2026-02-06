package com.yowyob.feedback.controller;

import com.yowyob.feedback.dto.response.UserResponseDTO;
import com.yowyob.feedback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import com.yowyob.feedback.util.SecurityUtil;

/**
 * REST controller for user management endpoints.
 * Provides endpoints to retrieve user information.
 *
 * Base path: /api/v1/users
 *
 * All endpoints require authentication via JWT token in Authorization header.
 *
 * @author Thomas Djotio Ndié
 * @since 2026-02-04
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User retrieval and management API")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    /**
     * Retrieves all users in the system except the current authenticated user.
     * Requires authentication.
     *
     * @return Flux<UserResponseDTO> stream of all users except current user
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all users",
            description = "Retrieves all users except the current authenticated user. Requires authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Flux<UserResponseDTO> getAllUsers() {
        log.info("GET /api/v1/users - Retrieving all users except current user");
        return SecurityUtil.getAuthenticatedUserId()
                .flatMapMany(currentUserId -> userService.getAllUsers(currentUserId));
    }

    /**
     * Retrieves all users of type PERSON except the current authenticated user.
     * Requires authentication.
     *
     * @return Flux<UserResponseDTO> stream of person-type users except current user
     */
    @GetMapping(value = "/persons", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all persons",
            description = "Retrieves all users of type PERSON except the current authenticated user. Requires authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Person users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Flux<UserResponseDTO> getAllPersons() {
        log.info("GET /api/v1/users/persons - Retrieving all PERSON users except current user");
        return SecurityUtil.getAuthenticatedUserId()
                .flatMapMany(currentUserId -> userService.getAllPersons(currentUserId));
    }

    /**
     * Retrieves all users of type ORGANIZATION except the current authenticated user.
     * Requires authentication.
     *
     * @return Flux<UserResponseDTO> stream of organization-type users except current user
     */
    @GetMapping(value = "/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all organizations",
            description = "Retrieves all users of type ORGANIZATION except the current authenticated user. Requires authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Organization users retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Flux<UserResponseDTO> getAllOrganizations() {
        log.info("GET /api/v1/users/organizations - Retrieving all ORGANIZATION users except current user");
        return SecurityUtil.getAuthenticatedUserId()
                .flatMapMany(currentUserId -> userService.getAllOrganizations(currentUserId));
    }
}

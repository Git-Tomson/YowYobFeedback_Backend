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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
     * Retrieves all users in the system.
     * Requires authentication.
     *
     * @return Flux<UserResponseDTO> stream of all users
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all users",
            description = "Retrieves all users regardless of type. Requires authentication."
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
        log.info("GET /api/v1/users - Retrieving all users");
        return userService.getAllUsers();
    }

    /**
     * Retrieves all users of type PERSON.
     * Requires authentication.
     *
     * @return Flux<UserResponseDTO> stream of person-type users
     */
    @GetMapping(value = "/persons", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all persons",
            description = "Retrieves all users of type PERSON with their occupation. Requires authentication."
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
        log.info("GET /api/v1/users/persons - Retrieving all PERSON users");
        return userService.getAllPersons();
    }

    /**
     * Retrieves all users of type ORGANIZATION.
     * Requires authentication.
     *
     * @return Flux<UserResponseDTO> stream of organization-type users
     */
    @GetMapping(value = "/organizations", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all organizations",
            description = "Retrieves all users of type ORGANIZATION with their location. Requires authentication."
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
        log.info("GET /api/v1/users/organizations - Retrieving all ORGANIZATION users");
        return userService.getAllOrganizations();
    }
}

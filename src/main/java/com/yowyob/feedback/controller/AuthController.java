package com.yowyob.feedback.controller;

import com.yowyob.feedback.dto.request.LoginRequestDTO;
import com.yowyob.feedback.dto.request.RegisterRequestDTO;
import com.yowyob.feedback.dto.response.AuthResponseDTO;
import com.yowyob.feedback.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for authentication endpoints.
 * Handles user registration and login operations.
 *
 * Base path: /api/v1/auth
 *
 * All endpoints return Mono for reactive non-blocking responses.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication API")
public class AuthController {

    private final AuthService auth_service;

    /**
     * Registers a new user account.
     *
     * At least one of email or contact must be provided.
     * Password will be encrypted before storage.
     *
     * @param register_request the registration data
     * @return Mono<AuthResponseDTO> containing the created user information
     */
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account. At least email or contact must be provided."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or user already exists"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO register_request) {
        log.info("POST /api/v1/auth/register - New user registration");
        return auth_service.register(register_request)
                .doOnNext(response -> log.info("Réponse émise : {}", response))
                .doOnTerminate(() -> log.info("Le flux est terminé"));
    }

    /**
     * Authenticates a user with credentials.
     *
     * Identifier can be either email or contact number.
     *
     * @param login_request the login credentials
     * @return Mono<AuthResponseDTO> containing the authenticated user information
     */
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "User login",
            description = "Authenticates user with email/contact and password"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid credentials"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO login_request) {
        log.info("POST /api/v1/auth/login - User login attempt");
        return auth_service.login(login_request);
    }
}

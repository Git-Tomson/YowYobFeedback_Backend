package com.yowyob.feedback.controller;

import com.yowyob.feedback.dto.request.LoginRequestDTO;
import com.yowyob.feedback.dto.request.RegisterRequestDTO;
import com.yowyob.feedback.dto.response.AuthResponseDTO;
import com.yowyob.feedback.dto.response.UserResponseDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import com.yowyob.feedback.dto.request.PasswordResetRequestDTO;
import com.yowyob.feedback.dto.request.PasswordResetConfirmDTO;
import com.yowyob.feedback.dto.request.TwoFactorVerifyDTO;
import com.yowyob.feedback.dto.response.TwoFactorSetupResponseDTO;
import com.yowyob.feedback.service.PasswordResetService;
import org.springframework.security.core.Authentication;
import java.util.Map;

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
    private final PasswordResetService password_reset_service;

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

    /**
     * Gets the currently authenticated user's information.
     *
     * @param authentication the authentication principal
     * @return Mono<UserResponseDTO> containing current user data
     */
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get current user",
            description = "Retrieves information about the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User information retrieved successfully",
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
    public Mono<UserResponseDTO> getCurrentUser(Authentication authentication) {
        log.info("GET /api/v1/auth/me - Fetching current user information");
        String identifier = authentication.getName();
        return auth_service.getCurrentUser(identifier);
    }

    /**
     * Logs out the current user.
     * In a stateless JWT authentication, this is mainly for client-side token invalidation.
     *
     * @return Mono<Map<String, String>> logout confirmation message
     */
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "User logout",
            description = "Logs out the current user. Client should discard the JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<Map<String, String>> logout() {
        log.info("POST /api/v1/auth/logout - User logout");
        return auth_service.logout();
    }

    /**
     * Initiates password reset process.
     *
     * @param request the password reset request containing email
     * @return Mono<Map<String, String>> success message
     */
    @PostMapping(value = "/password-reset/request", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Request password reset",
            description = "Sends a password reset email to the user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset email sent"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid email"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<Map<String, String>> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestDTO request) {
        log.info("POST /api/v1/auth/password-reset/request - Password reset requested");
        return password_reset_service.initiatePasswordReset(request.email())
                .map(message -> Map.of("message", message));
    }

    /**
     * Confirms password reset with token and new password.
     *
     * @param request the confirmation request containing token and new password
     * @return Mono<Map<String, String>> success message
     */
    @PostMapping(value = "/password-reset/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Confirm password reset",
            description = "Sets a new password using the reset token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successful"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired token"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<Map<String, String>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmDTO request) {
        log.info("POST /api/v1/auth/password-reset/confirm - Password reset confirmation");
        return password_reset_service.confirmPasswordReset(request.token(), request.new_password())
                .map(message -> Map.of("message", message));
    }

    /**
     * Enables two-factor authentication for the current user.
     *
     * @param authentication the authentication principal
     * @return Mono<TwoFactorSetupResponseDTO> containing QR code and backup codes
     */
    @PostMapping(value = "/2fa/enable", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Enable 2FA",
            description = "Enables two-factor authentication for the user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "2FA enabled successfully",
                    content = @Content(schema = @Schema(implementation = TwoFactorSetupResponseDTO.class))
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
    public Mono<TwoFactorSetupResponseDTO> enableTwoFactor(Authentication authentication) {
        log.info("POST /api/v1/auth/2fa/enable - Enabling 2FA");
        String identifier = authentication.getName();
        return auth_service.getUserIdByIdentifier(identifier)
                .flatMap(auth_service::enableTwoFactor);
    }

    /**
     * Disables two-factor authentication for the current user.
     *
     * @param authentication the authentication principal
     * @return Mono<Map<String, String>> success message
     */
    @PostMapping(value = "/2fa/disable", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Disable 2FA",
            description = "Disables two-factor authentication for the user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "2FA disabled successfully"
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
    public Mono<Map<String, String>> disableTwoFactor(Authentication authentication) {
        log.info("POST /api/v1/auth/2fa/disable - Disabling 2FA");
        String identifier = authentication.getName();
        return auth_service.getUserIdByIdentifier(identifier)
                .flatMap(auth_service::disableTwoFactor)
                .map(message -> Map.of("message", message));
    }

    /**
     * Verifies two-factor authentication code.
     *
     * @param request the 2FA verification request
     * @return Mono<AuthResponseDTO> authentication response if successful
     */
    @PostMapping(value = "/2fa/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Verify 2FA code",
            description = "Verifies the 2FA code and completes login"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "2FA verification successful",
                    content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid 2FA code"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<AuthResponseDTO> verifyTwoFactor(@Valid @RequestBody TwoFactorVerifyDTO request) {
        log.info("POST /api/v1/auth/2fa/verify - 2FA verification");
        return auth_service.verifyTwoFactorCode(request.identifier(), request.code());
    }
}

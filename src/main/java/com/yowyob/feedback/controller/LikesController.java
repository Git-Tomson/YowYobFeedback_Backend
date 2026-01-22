package com.yowyob.feedback.controller;

import com.yowyob.feedback.dto.request.CreateLikeRequestDTO;
import com.yowyob.feedback.dto.response.LikeResponseDTO;
import com.yowyob.feedback.service.LikesService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.UUID;

/**
 * REST controller for like endpoints.
 * Handles like creation, retrieval, and deletion operations on feedbacks.
 *
 * Base path: /api/v1/likes
 *
 * All endpoints return Mono or Flux for reactive non-blocking responses.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
@Tag(name = "Likes", description = "Like management API for feedbacks")
public class LikesController {

    private final LikesService likes_service;

    /**
     * Creates a new like on a feedback.
     * The liker is extracted from the JWT token in the Authorization header.
     *
     * @param request the like creation request
     * @param http_request the HTTP request containing headers
     * @return Mono<LikeResponseDTO>
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new like",
            description = "Creates a new like on a feedback. User is extracted from JWT token.",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Like created successfully",
                    content = @Content(schema = @Schema(implementation = LikeResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or already liked"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Feedback not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<LikeResponseDTO> createLike(
            @Valid @RequestBody CreateLikeRequestDTO request,
            ServerHttpRequest http_request) {
        log.info("POST /api/v1/likes - Creating like");
        String authorization = http_request.getHeaders().getFirst("Authorization");
        return likes_service.createLike(request, authorization);
    }

    /**
     * Retrieves a specific like by feedback ID and liker ID.
     *
     * @param feedback_id the feedback ID
     * @param liker_id the liker ID
     * @return Mono<LikeResponseDTO>
     */
    @GetMapping(value = "/feedback/{feedbackId}/liker/{likerId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get like by feedback and liker",
            description = "Retrieves a specific like by feedback ID and liker ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Like retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LikeResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Like not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<LikeResponseDTO> getLikeByFeedbackAndLiker(
            @PathVariable("feedbackId") UUID feedback_id,
            @PathVariable("likerId") UUID liker_id) {
        log.info("GET /api/v1/likes/feedback/{}/liker/{} - Retrieving like",
                feedback_id, liker_id);
        return likes_service.getLikeByFeedbackAndLiker(feedback_id, liker_id);
    }

    /**
     * Retrieves all likes for a feedback.
     *
     * @param feedback_id the feedback ID
     * @return Flux<LikeResponseDTO>
     */
    @GetMapping(value = "/feedback/{feedbackId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get likes by feedback",
            description = "Retrieves all likes for a specific feedback"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Likes retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LikeResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Feedback not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Flux<LikeResponseDTO> getLikesByFeedback(
            @PathVariable("feedbackId") UUID feedback_id) {
        log.info("GET /api/v1/likes/feedback/{} - Retrieving likes", feedback_id);
        return likes_service.getLikesByFeedback(feedback_id);
    }

    /**
     * Retrieves all likes by a user.
     *
     * @param liker_id the liker ID
     * @return Flux<LikeResponseDTO>
     */
    @GetMapping(value = "/user/{likerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get likes by user",
            description = "Retrieves all likes made by a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Likes retrieved successfully",
                    content = @Content(schema = @Schema(implementation = LikeResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Flux<LikeResponseDTO> getLikesByUser(@PathVariable("likerId") UUID liker_id) {
        log.info("GET /api/v1/likes/user/{} - Retrieving likes", liker_id);
        return likes_service.getLikesByUser(liker_id);
    }

    /**
     * Deletes a like.
     * Only the user who created the like can delete it.
     *
     * @param feedback_id the feedback ID
     * @param http_request the HTTP request containing headers
     * @return Mono<Void>
     */
    @DeleteMapping("/feedback/{feedbackId}/liker")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete like",
            description = "Deletes an existing like. Only the liker can delete it.",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Like deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Like not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<Void> deleteLike(
            @PathVariable("feedbackId") UUID feedback_id,
            ServerHttpRequest http_request) {
        log.info("DELETE /api/v1/likes/feedback/{}/liker - Deleting like", feedback_id);
        String authorization = http_request.getHeaders().getFirst("Authorization");
        return likes_service.deleteLike(feedback_id, authorization);
    }
}

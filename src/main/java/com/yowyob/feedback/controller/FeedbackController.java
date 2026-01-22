package com.yowyob.feedback.controller;

import com.yowyob.feedback.dto.request.CreateFeedbackRequestDTO;
import com.yowyob.feedback.dto.request.UpdateFeedbackRequestDTO;
import com.yowyob.feedback.dto.response.FeedbackResponseDTO;
import com.yowyob.feedback.service.FeedbackService;
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
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for feedback endpoints.
 * Handles feedback creation, retrieval, update, and deletion operations.
 *
 * Base path: /api/v1/feedbacks
 *
 * All endpoints return Mono or Flux for reactive non-blocking responses.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Feedbacks", description = "Feedback management API")
public class FeedbackController {

    private final FeedbackService feedback_service;

    /**
     * Creates a new feedback.
     *
     * @param request the feedback creation request
     * @return Mono<FeedbackResponseDTO>
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new feedback",
            description = "Creates a new feedback for a project by a member"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Feedback created successfully",
                    content = @Content(schema = @Schema(implementation = FeedbackResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or member not found"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<FeedbackResponseDTO> createFeedback(
            @Valid @RequestBody CreateFeedbackRequestDTO request) {
        log.info("POST /api/v1/feedbacks - Creating feedback");
        return feedback_service.createFeedback(request);
    }

    /**
     * Retrieves a specific feedback by ID.
     *
     * @param feedback_id the feedback ID
     * @return Mono<FeedbackResponseDTO>
     */
    @GetMapping(value = "/{feedbackId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get feedback by ID",
            description = "Retrieves a specific feedback by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Feedback retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FeedbackResponseDTO.class))
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
    public Mono<FeedbackResponseDTO> getFeedbackById(@PathVariable("feedbackId") UUID feedback_id) {
        log.info("GET /api/v1/feedbacks/{} - Retrieving feedback", feedback_id);
        return feedback_service.getFeedbackById(feedback_id);
    }

    /**
     * Retrieves all feedbacks for a project.
     *
     * @param project_id the project ID
     * @return Flux<FeedbackResponseDTO>
     */
    @GetMapping(value = "/project/{projectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get feedbacks by project",
            description = "Retrieves all feedbacks for a specific project"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Feedbacks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FeedbackResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Flux<FeedbackResponseDTO> getFeedbacksByProject(
            @PathVariable("projectId") UUID project_id) {
        log.info("GET /api/v1/feedbacks/project/{} - Retrieving feedbacks", project_id);
        return feedback_service.getFeedbacksByProject(project_id);
    }

    /**
     * Retrieves feedbacks by member pseudo within a project.
     *
     * @param member_pseudo the member pseudo
     * @param project_id the project ID
     * @return Flux<FeedbackResponseDTO>
     */
    @GetMapping(value = "/member/{memberPseudo}/project/{projectId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get feedbacks by member and project",
            description = "Retrieves all feedbacks by a specific member within a project"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Feedbacks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FeedbackResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Flux<FeedbackResponseDTO> getFeedbacksByMemberAndProject(
            @PathVariable("memberPseudo") String member_pseudo,
            @PathVariable("projectId") UUID project_id) {
        log.info("GET /api/v1/feedbacks/member/{}/project/{} - Retrieving feedbacks",
                member_pseudo, project_id);
        return feedback_service.getFeedbacksByMemberAndProject(member_pseudo, project_id);
    }

    /**
     * Retrieves all feedbacks from projects owned by a user.
     *
     * @param user_id the user ID
     * @return Flux<FeedbackResponseDTO>
     */
    @GetMapping(value = "/user/{userId}/projects", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get feedbacks by user projects",
            description = "Retrieves all feedbacks from projects owned by a user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Feedbacks retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FeedbackResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Flux<FeedbackResponseDTO> getFeedbacksByUserProjects(
            @PathVariable("userId") UUID user_id) {
        log.info("GET /api/v1/feedbacks/user/{}/projects - Retrieving feedbacks", user_id);
        return feedback_service.getFeedbacksByUserProjects(user_id);
    }

    /**
     * Updates an existing feedback.
     * Only the member who created the feedback can update it.
     *
     * @param feedback_id the feedback ID
     * @param request the update request
     * @param http_request the HTTP request containing headers
     * @return Mono<FeedbackResponseDTO>
     */
    @PutMapping(value = "/{feedbackId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update feedback",
            description = "Updates an existing feedback. Only the creator can update it.",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Feedback updated successfully",
                    content = @Content(schema = @Schema(implementation = FeedbackResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - You are not the owner"
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
    public Mono<FeedbackResponseDTO> updateFeedback(
            @PathVariable("feedbackId") UUID feedback_id,
            @Valid @RequestBody UpdateFeedbackRequestDTO request,
            ServerHttpRequest http_request) {
        log.info("PUT /api/v1/feedbacks/{} - Updating feedback", feedback_id);
        String authorization = http_request.getHeaders().getFirst("Authorization");
        return feedback_service.updateFeedback(feedback_id, request, authorization);
    }

    /**
     * Deletes a feedback.
     * Only the member who created the feedback can delete it.
     *
     * @param feedback_id the feedback ID
     * @param http_request the HTTP request containing headers
     * @return Mono<Void>
     */
    @DeleteMapping("/{feedbackId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete feedback",
            description = "Deletes an existing feedback. Only the creator can delete it.",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Feedback deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - You are not the owner"
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
    public Mono<Void> deleteFeedback(
            @PathVariable("feedbackId") UUID feedback_id,
            ServerHttpRequest http_request) {
        log.info("DELETE /api/v1/feedbacks/{} - Deleting feedback", feedback_id);
        String authorization = http_request.getHeaders().getFirst("Authorization");
        return feedback_service.deleteFeedback(feedback_id, authorization);
    }

}

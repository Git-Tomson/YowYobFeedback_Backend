package com.yowyob.feedback.controller;

import com.yowyob.feedback.dto.request.CreateCommentRequestDTO;
import com.yowyob.feedback.dto.request.UpdateCommentRequestDTO;
import com.yowyob.feedback.dto.response.CommentResponseDTO;
import com.yowyob.feedback.service.CommentService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller for comment endpoints.
 * Handles comment creation, retrieval, update, and deletion operations.
 *
 * Base path: /api/v1/comments
 *
 * All endpoints return Mono or Flux for reactive non-blocking responses.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management API")
public class CommentController {

    private final CommentService comment_service;

    /**
     * Creates a new comment.
     *
     * @param request the comment creation request
     * @return Mono<CommentResponseDTO>
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new comment",
            description = "Creates a new comment on a feedback"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment created successfully",
                    content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Feedback or user not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<CommentResponseDTO> createComment(
            @Valid @RequestBody CreateCommentRequestDTO request) {
        log.info("POST /api/v1/comments - Creating comment");
        return comment_service.createComment(request);
    }

    /**
     * Retrieves a specific comment by ID.
     *
     * @param comment_id the comment ID
     * @return Mono<CommentResponseDTO>
     */
    @GetMapping(value = "/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get comment by ID",
            description = "Retrieves a specific comment by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<CommentResponseDTO> getCommentById(@PathVariable("commentId") UUID comment_id) {
        log.info("GET /api/v1/comments/{} - Retrieving comment", comment_id);
        return comment_service.getCommentById(comment_id);
    }

    /**
     * Retrieves all comments for a feedback.
     *
     * @param feedback_id the feedback ID
     * @return Flux<CommentResponseDTO>
     */
    @GetMapping(value = "/feedback/{feedbackId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get comments by feedback",
            description = "Retrieves all comments for a specific feedback"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))
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
    public Flux<CommentResponseDTO> getCommentsByFeedback(
            @PathVariable("feedbackId") UUID feedback_id) {
        log.info("GET /api/v1/comments/feedback/{} - Retrieving comments", feedback_id);
        return comment_service.getCommentsByFeedback(feedback_id);
    }

    /**
     * Updates an existing comment.
     *
     * @param comment_id the comment ID
     * @param request the update request
     * @return Mono<CommentResponseDTO>
     */
    @PutMapping(value = "/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update comment",
            description = "Updates an existing comment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment updated successfully",
                    content = @Content(schema = @Schema(implementation = CommentResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<CommentResponseDTO> updateComment(
            @PathVariable("commentId") UUID comment_id,
            @Valid @RequestBody UpdateCommentRequestDTO request) {
        log.info("PUT /api/v1/comments/{} - Updating comment", comment_id);
        return comment_service.updateComment(comment_id, request);
    }

    /**
     * Deletes a comment.
     *
     * @param comment_id the comment ID
     * @return Mono<Void>
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete comment",
            description = "Deletes an existing comment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Comment deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comment not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<Void> deleteComment(@PathVariable("commentId") UUID comment_id) {
        log.info("DELETE /api/v1/comments/{} - Deleting comment", comment_id);
        return comment_service.deleteComment(comment_id);
    }
}

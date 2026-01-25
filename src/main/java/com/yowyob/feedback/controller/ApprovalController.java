package com.yowyob.feedback.controller;

import com.yowyob.feedback.dto.request.CreateApprovalRequestDTO;
import com.yowyob.feedback.dto.response.ApprovalResponseDTO;
import com.yowyob.feedback.service.ApprovalService;
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
 * REST controller for approval endpoints.
 * Handles approval creation, retrieval, and deletion operations on comments.
 *
 * Base path: /api/v1/approvals
 *
 * All endpoints return Mono or Flux for reactive non-blocking responses.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Approvals", description = "Approval management API for comments")
public class ApprovalController {

    private final ApprovalService approval_service;

    /**
     * Creates a new approval on a comment.
     * The approver is extracted from the JWT token in the Authorization header.
     *
     * @param request the approval creation request
     * @param http_request the HTTP request containing headers
     * @return Mono<ApprovalResponseDTO>
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new approval",
            description = "Creates a new approval on a comment. User is extracted from JWT token.",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Approval created successfully",
                    content = @Content(schema = @Schema(implementation = ApprovalResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or already approved"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
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
    public Mono<ApprovalResponseDTO> createApproval(
            @Valid @RequestBody CreateApprovalRequestDTO request,
            ServerHttpRequest http_request) {
        log.info("POST /api/v1/approvals - Creating approval");
        String authorization = http_request.getHeaders().getFirst("Authorization");
        return approval_service.createApproval(request, authorization);
    }

    /**
     * Retrieves a specific approval by comment ID and approver ID.
     *
     * @param comments_id the comment ID
     * @param approver_id the approver ID
     * @return Mono<ApprovalResponseDTO>
     */
    @GetMapping(value = "/comment/{commentId}/approver/{approverId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get approval by comment and approver",
            description = "Retrieves a specific approval by comment ID and approver ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Approval retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApprovalResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Approval not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<ApprovalResponseDTO> getApprovalByCommentAndApprover(
            @PathVariable("commentId") UUID comments_id,
            @PathVariable("approverId") UUID approver_id) {
        log.info("GET /api/v1/approvals/comment/{}/approver/{} - Retrieving approval",
                comments_id, approver_id);
        return approval_service.getApprovalByCommentAndApprover(comments_id, approver_id);
    }

    /**
     * Retrieves all approvals for a comment.
     *
     * @param comments_id the comment ID
     * @return Flux<ApprovalResponseDTO>
     */
    @GetMapping(value = "/comment/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get approvals by comment",
            description = "Retrieves all approvals for a specific comment"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Approvals retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApprovalResponseDTO.class))
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
    public Flux<ApprovalResponseDTO> getApprovalsByComment(
            @PathVariable("commentId") UUID comments_id) {
        log.info("GET /api/v1/approvals/comment/{} - Retrieving approvals", comments_id);
        return approval_service.getApprovalsByComment(comments_id);
    }

    /**
     * Retrieves all approvals by a user.
     *
     * @param approver_id the approver ID
     * @return Flux<ApprovalResponseDTO>
     */
    @GetMapping(value = "/user/{approverId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get approvals by user",
            description = "Retrieves all approvals made by a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Approvals retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApprovalResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Flux<ApprovalResponseDTO> getApprovalsByUser(
            @PathVariable("approverId") UUID approver_id) {
        log.info("GET /api/v1/approvals/user/{} - Retrieving approvals", approver_id);
        return approval_service.getApprovalsByUser(approver_id);
    }

    /**
     * Deletes an approval.
     * Only the user who created the approval can delete it.
     *
     * @param comments_id the comment ID
     * @param http_request the HTTP request containing headers
     * @return Mono<Void>
     */
    @DeleteMapping("/comment/{commentId}/approver")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete approval",
            description = "Deletes an existing approval. Only the approver can delete it.",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Approval deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Approval not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Mono<Void> deleteApproval(
            @PathVariable("commentId") UUID comments_id,
            ServerHttpRequest http_request) {
        log.info("DELETE /api/v1/approvals/comment/{}/approver - Deleting approval", comments_id);
        String authorization = http_request.getHeaders().getFirst("Authorization");
        return approval_service.deleteApproval(comments_id, authorization);
    }
}

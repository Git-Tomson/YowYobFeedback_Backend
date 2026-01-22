package com.yowyob.feedback.service;

import com.yowyob.feedback.dto.request.CreateApprovalRequestDTO;
import com.yowyob.feedback.dto.response.ApprovalResponseDTO;
import com.yowyob.feedback.entity.Approval;
import com.yowyob.feedback.mapper.ApprovalMapper;
import com.yowyob.feedback.repository.ApprovalRepository;
import com.yowyob.feedback.repository.AppUserRepository;
import com.yowyob.feedback.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service class handling approval operations on comments.
 * Manages approval creation, retrieval, and deletion.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String APPROVAL_NOT_FOUND_MESSAGE = "Approval not found";
    private static final String COMMENT_NOT_FOUND_MESSAGE = "Comment not found";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String APPROVAL_ALREADY_EXISTS_MESSAGE = "You have already approved this comment";

    private final ApprovalRepository approval_repository;
    private final CommentRepository comment_repository;
    private final AppUserRepository app_user_repository;
    private final ApprovalMapper approval_mapper;
    private final JwtService jwt_service;

    /**
     * Creates a new approval on a comment.
     * The approver is extracted from the JWT token.
     *
     * @param request the approval creation request
     * @param authorization_header the Authorization header containing JWT token
     * @return Mono<ApprovalResponseDTO>
     * @throws IllegalArgumentException if comment doesn't exist or already approved
     */
    @Transactional
    public Mono<ApprovalResponseDTO> createApproval(CreateApprovalRequestDTO request,
                                                    String authorization_header) {
        log.info("Creating approval for comment: {}", request.comments_id());

        return extractUserIdFromToken(authorization_header)
                .flatMap(approver_id -> {
                    log.info("Approver ID from token: {}", approver_id);

                    return validateCommentExists(request.comments_id())
                            .then(validateUserExists(approver_id))
                            .then(checkApprovalDoesNotExist(request.comments_id(), approver_id))
                            .then(saveApproval(request, approver_id))
                            .flatMap(saved_approval ->
                                    incrementCommentLikeCount(request.comments_id())
                                            .thenReturn(saved_approval)
                            )
                            .flatMap(this::buildResponseWithApproverName);
                })
                .doOnSuccess(response -> log.info("Approval created successfully for comment: {}",
                        request.comments_id()))
                .doOnError(error -> log.error("Failed to create approval for comment: {}",
                        request.comments_id(), error));
    }

    /**
     * Retrieves a specific approval by comment ID and approver ID.
     *
     * @param comments_id the comment ID
     * @param approver_id the approver ID
     * @return Mono<ApprovalResponseDTO>
     */
    public Mono<ApprovalResponseDTO> getApprovalByCommentAndApprover(UUID comments_id,
                                                                     UUID approver_id) {
        log.info("Retrieving approval for comment: {}, approver: {}", comments_id, approver_id);

        return approval_repository.findByCommentsIdAndApproverId(comments_id, approver_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(APPROVAL_NOT_FOUND_MESSAGE)))
                .flatMap(this::buildResponseWithApproverName)
                .doOnSuccess(response -> log.info("Approval retrieved successfully"))
                .doOnError(error -> log.error("Failed to retrieve approval", error));
    }

    /**
     * Retrieves all approvals for a comment.
     *
     * @param comments_id the comment ID
     * @return Flux<ApprovalResponseDTO>
     */
    public Flux<ApprovalResponseDTO> getApprovalsByComment(UUID comments_id) {
        log.info("Retrieving all approvals for comment: {}", comments_id);

        return validateCommentExists(comments_id)
                .thenMany(approval_repository.findByCommentsId(comments_id)
                        .flatMap(this::buildResponseWithApproverName)
                )
                .doOnComplete(() -> log.info("All approvals retrieved for comment: {}", comments_id))
                .doOnError(error -> log.error("Failed to retrieve approvals for comment: {}",
                        comments_id, error));
    }

    /**
     * Retrieves all approvals by a user.
     *
     * @param approver_id the approver ID
     * @return Flux<ApprovalResponseDTO>
     */
    public Flux<ApprovalResponseDTO> getApprovalsByUser(UUID approver_id) {
        log.info("Retrieving all approvals by user: {}", approver_id);

        return validateUserExists(approver_id)
                .thenMany(approval_repository.findByApproverId(approver_id)
                        .flatMap(this::buildResponseWithApproverName)
                )
                .doOnComplete(() -> log.info("All approvals retrieved for user: {}", approver_id))
                .doOnError(error -> log.error("Failed to retrieve approvals for user: {}",
                        approver_id, error));
    }

    /**
     * Deletes an approval.
     * Only the user who created the approval can delete it (verified via JWT token).
     *
     * @param comments_id the comment ID
     * @param authorization_header the Authorization header containing JWT token
     * @return Mono<Void>
     */
    @Transactional
    public Mono<Void> deleteApproval(UUID comments_id, String authorization_header) {
        log.info("Deleting approval for comment: {}", comments_id);

        return extractUserIdFromToken(authorization_header)
                .flatMap(approver_id -> {
                    log.info("Deleting approval from user: {}", approver_id);

                    return approval_repository.findByCommentsIdAndApproverId(comments_id, approver_id)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(APPROVAL_NOT_FOUND_MESSAGE)))
                            .flatMap(approval ->
                                    approval_repository.deleteByCommentsIdAndApproverId(comments_id, approver_id)
                                            .then(decrementCommentLikeCount(comments_id))
                            );
                })
                .doOnSuccess(v -> log.info("Approval deleted successfully for comment: {}", comments_id))
                .doOnError(error -> log.error("Failed to delete approval for comment: {}",
                        comments_id, error));
    }

    private Mono<UUID> extractUserIdFromToken(String authorization_header) {
        if (authorization_header == null || !authorization_header.startsWith(BEARER_PREFIX)) {
            return Mono.error(new IllegalArgumentException("Invalid authorization header"));
        }

        String token = authorization_header.substring(BEARER_PREFIX.length());

        return jwt_service.validateTokenAndExtractUserId(token)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(USER_NOT_FOUND_MESSAGE)));
    }

    private Mono<Void> validateCommentExists(UUID comments_id) {
        return comment_repository.findById(comments_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(COMMENT_NOT_FOUND_MESSAGE)))
                .then();
    }

    private Mono<Void> validateUserExists(UUID user_id) {
        return app_user_repository.findById(user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(USER_NOT_FOUND_MESSAGE)))
                .then();
    }

    private Mono<Void> checkApprovalDoesNotExist(UUID comments_id, UUID approver_id) {
        return approval_repository.existsByCommentsIdAndApproverId(comments_id, approver_id)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new IllegalArgumentException(APPROVAL_ALREADY_EXISTS_MESSAGE));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Approval> saveApproval(CreateApprovalRequestDTO request, UUID approver_id) {
        Approval approval = approval_mapper.toEntity(request, approver_id);
        return approval_repository.save(approval);
    }

    private Mono<Void> incrementCommentLikeCount(UUID comments_id) {
        return comment_repository.incrementApprovalCount(comments_id);
    }

    private Mono<Void> decrementCommentLikeCount(UUID comments_id) {
        return comment_repository.decrementApprovalCount(comments_id);
    }

    private Mono<ApprovalResponseDTO> buildResponseWithApproverName(Approval approval) {
        return app_user_repository.findById(approval.getApprover_id())
                .map(user -> {
                    String approver_name = user.getUser_firstname() != null
                            ? user.getUser_firstname() + " " + user.getUser_lastname()
                            : user.getUser_lastname();
                    return approval_mapper.toResponseDTOWithName(approval, approver_name);
                })
                .defaultIfEmpty(approval_mapper.toResponseDTOWithName(approval, "Unknown User"));
    }
}

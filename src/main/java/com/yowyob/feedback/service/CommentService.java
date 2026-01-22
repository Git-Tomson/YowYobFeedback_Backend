package com.yowyob.feedback.service;

import com.yowyob.feedback.dto.request.CreateCommentRequestDTO;
import com.yowyob.feedback.dto.request.UpdateCommentRequestDTO;
import com.yowyob.feedback.dto.response.CommentResponseDTO;
import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.Comment;
import com.yowyob.feedback.mapper.CommentMapper;
import com.yowyob.feedback.repository.AppUserRepository;
import com.yowyob.feedback.repository.CommentRepository;
import com.yowyob.feedback.repository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.yowyob.feedback.constant.AppConstants.FORBIDDEN_DELETE_COMMENT_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.FORBIDDEN_UPDATE_COMMENT_MESSAGE;

/**
 * Service class handling comment operations.
 * Manages comment creation, retrieval, update, and deletion.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private static final String COMMENT_NOT_FOUND_MESSAGE = "Comment not found";
    private static final String FEEDBACK_NOT_FOUND_MESSAGE = "Feedback not found";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    private final CommentRepository comment_repository;
    private final FeedbackRepository feedback_repository;
    private final AppUserRepository app_user_repository;
    private final CommentMapper comment_mapper;
    private final JwtService jwt_service;

    /**
     * Creates a new comment.
     *
     * @param request the comment creation request
     * @return Mono<CommentResponseDTO>
     */
    @Transactional
    public Mono<CommentResponseDTO> createComment(CreateCommentRequestDTO request) {
        log.info("Creating comment for feedback: {}, commenter: {}",
                request.feedback_id(), request.commenter_id());

        return validateFeedbackExists(request.feedback_id())
                .then(validateUserExists(request.commenter_id()))
                .then(Mono.defer(() -> {
                    Comment comment = comment_mapper.toEntity(request);
                    return comment_repository.save(comment);
                }))
                .flatMap(saved_comment ->
                        feedback_repository.incrementCommentCount(request.feedback_id())
                                .thenReturn(saved_comment)
                )
                .flatMap(this::buildResponseWithCommenterName)
                .doOnSuccess(response -> log.info("Comment created: {}", response.comments_id()))
                .doOnError(error -> log.error("Failed to create comment: {}", error.getMessage()));
    }

    /**
     * Retrieves a specific comment by ID.
     *
     * @param comment_id the comment ID
     * @return Mono<CommentResponseDTO>
     */
    public Mono<CommentResponseDTO> getCommentById(UUID comment_id) {
        log.info("Retrieving comment: {}", comment_id);

        return comment_repository.findById(comment_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(COMMENT_NOT_FOUND_MESSAGE)))
                .flatMap(this::buildResponseWithCommenterName);
    }

    /**
     * Retrieves all comments for a feedback.
     *
     * @param feedback_id the feedback ID
     * @return Flux<CommentResponseDTO>
     */
    public Flux<CommentResponseDTO> getCommentsByFeedback(UUID feedback_id) {
        log.info("Retrieving comments for feedback: {}", feedback_id);

        return validateFeedbackExists(feedback_id)
                .thenMany(comment_repository.findByFeedbackId(feedback_id))
                .flatMap(this::buildResponseWithCommenterName);
    }

    /**
     * Updates an existing comment.
     * Only the user who created the comment can update it.
     *
     * @param comment_id the comment ID
     * @param request the update request
     * @param authorization_header the Authorization header containing JWT token
     * @return Mono<CommentResponseDTO>
     * @throws IllegalArgumentException if user is not authorized or comment not found
     */
    @Transactional
    public Mono<CommentResponseDTO> updateComment(UUID comment_id,
                                                  UpdateCommentRequestDTO request,
                                                  String authorization_header) {
        log.info("Updating comment: {}", comment_id);

        return extractUserIdFromToken(authorization_header)
                .flatMap(current_user_id ->
                        comment_repository.findById(comment_id)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(COMMENT_NOT_FOUND_MESSAGE)))
                                .flatMap(comment -> validateCommentOwnershipForUpdate(comment, current_user_id))
                                .flatMap(comment -> {
                                    comment_mapper.updateEntity(comment, request);
                                    return comment_repository.save(comment);
                                })
                                .flatMap(this::buildResponseWithCommenterName)
                )
                .doOnSuccess(response -> log.info("Comment updated: {}", comment_id))
                .doOnError(error -> log.error("Failed to update comment: {}", comment_id, error));
    }

    private Mono<Comment> validateCommentOwnershipForUpdate(Comment comment, UUID current_user_id) {
        if (!comment.getCommenter_id().equals(current_user_id)) {
            return Mono.error(new IllegalArgumentException(FORBIDDEN_UPDATE_COMMENT_MESSAGE));
        }
        return Mono.just(comment);
    }

    /**
     * Deletes a comment.
     * Only the user who created the comment can delete it.
     *
     * @param comment_id the comment ID
     * @param authorization_header the Authorization header containing JWT token
     * @return Mono<Void>
     * @throws IllegalArgumentException if user is not authorized or comment not found
     */
    @Transactional
    public Mono<Void> deleteComment(UUID comment_id, String authorization_header) {
        log.info("Deleting comment: {}", comment_id);

        return extractUserIdFromToken(authorization_header)
                .flatMap(current_user_id ->
                        comment_repository.findById(comment_id)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(COMMENT_NOT_FOUND_MESSAGE)))
                                .flatMap(comment -> validateCommentOwnership(comment, current_user_id))
                                .flatMap(comment ->
                                        comment_repository.deleteById(comment_id)
                                                .then(feedback_repository.decrementCommentCount(comment.getFeedback_id()))
                                )
                )
                .doOnSuccess(v -> log.info("Comment deleted: {}", comment_id))
                .doOnError(error -> log.error("Failed to delete comment: {}", comment_id, error));
    }

    /**
     * Validates that the current user is the owner of the comment.
     *
     * @param comment the comment to validate
     * @param current_user_id the current user ID from JWT token
     * @return Mono<Comment> if validation succeeds
     * @throws IllegalArgumentException if user is not the owner
     */
    private Mono<Comment> validateCommentOwnership(Comment comment, UUID current_user_id) {
        if (!comment.getCommenter_id().equals(current_user_id)) {
            return Mono.error(new IllegalArgumentException(FORBIDDEN_DELETE_COMMENT_MESSAGE));
        }
        return Mono.just(comment);
    }

    /**
     * Extracts user ID from JWT token.
     *
     * @param authorization_header the Authorization header
     * @return Mono<UUID> the user ID
     */
    private Mono<UUID> extractUserIdFromToken(String authorization_header) {
        if (authorization_header == null || !authorization_header.startsWith("Bearer ")) {
            return Mono.error(new IllegalArgumentException("Invalid authorization header"));
        }

        String token = authorization_header.substring("Bearer ".length());

        return jwt_service.validateTokenAndExtractUserId(token)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")));
    }

    private Mono<Void> validateFeedbackExists(UUID feedback_id) {
        return feedback_repository.findById(feedback_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(FEEDBACK_NOT_FOUND_MESSAGE)))
                .then();
    }

    private Mono<Void> validateUserExists(UUID user_id) {
        return app_user_repository.findById(user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(USER_NOT_FOUND_MESSAGE)))
                .then();
    }

    private Mono<CommentResponseDTO> buildResponseWithCommenterName(Comment comment) {
        return app_user_repository.findById(comment.getCommenter_id())
                .map(user -> {
                    String commenter_name = user.getUser_firstname() != null
                            ? user.getUser_firstname() + " " + user.getUser_lastname()
                            : user.getUser_lastname();
                    return comment_mapper.toResponseDTOWithName(comment, commenter_name);
                })
                .defaultIfEmpty(comment_mapper.toResponseDTOWithName(comment, "Unknown User"));
    }
}

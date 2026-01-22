package com.yowyob.feedback.service;

import com.yowyob.feedback.dto.request.CreateLikeRequestDTO;
import com.yowyob.feedback.dto.response.LikeResponseDTO;
import com.yowyob.feedback.entity.Likes;
import com.yowyob.feedback.mapper.LikesMapper;
import com.yowyob.feedback.repository.AppUserRepository;
import com.yowyob.feedback.repository.FeedbackRepository;
import com.yowyob.feedback.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service class handling like operations on feedbacks.
 * Manages like creation, retrieval, and deletion.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikesService {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String LIKE_NOT_FOUND_MESSAGE = "Like not found";
    private static final String FEEDBACK_NOT_FOUND_MESSAGE = "Feedback not found";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";
    private static final String LIKE_ALREADY_EXISTS_MESSAGE = "You have already liked this feedback";

    private final LikeRepository like_repository;
    private final FeedbackRepository feedback_repository;
    private final AppUserRepository app_user_repository;
    private final LikesMapper likes_mapper;
    private final JwtService jwt_service;

    /**
     * Creates a new like on a feedback.
     * The liker is extracted from the JWT token.
     *
     * @param request the like creation request
     * @param authorization_header the Authorization header containing JWT token
     * @return Mono<LikeResponseDTO>
     * @throws IllegalArgumentException if feedback doesn't exist or already liked
     */
    @Transactional
    public Mono<LikeResponseDTO> createLike(CreateLikeRequestDTO request,
                                            String authorization_header) {
        log.info("Creating like for feedback: {}", request.feedback_id());

        return extractUserIdFromToken(authorization_header)
                .flatMap(liker_id -> {
                    log.info("Liker ID from token: {}", liker_id);

                    return validateFeedbackExists(request.feedback_id())
                            .then(validateUserExists(liker_id))
                            .then(checkLikeDoesNotExist(request.feedback_id(), liker_id))
                            .then(saveLike(request, liker_id))
                            .flatMap(saved_like ->
                                    incrementFeedbackLikeCount(request.feedback_id())
                                            .thenReturn(saved_like)
                            )
                            .flatMap(this::buildResponseWithLikerName);
                })
                .doOnSuccess(response -> log.info("Like created successfully for feedback: {}",
                        request.feedback_id()))
                .doOnError(error -> log.error("Failed to create like for feedback: {}",
                        request.feedback_id(), error));
    }

    /**
     * Retrieves a specific like by feedback ID and liker ID.
     *
     * @param feedback_id the feedback ID
     * @param liker_id the liker ID
     * @return Mono<LikeResponseDTO>
     */
    public Mono<LikeResponseDTO> getLikeByFeedbackAndLiker(UUID feedback_id, UUID liker_id) {
        log.info("Retrieving like for feedback: {}, liker: {}", feedback_id, liker_id);

        return like_repository.findByFeedbackIdAndLikerId(feedback_id, liker_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(LIKE_NOT_FOUND_MESSAGE)))
                .flatMap(this::buildResponseWithLikerName)
                .doOnSuccess(response -> log.info("Like retrieved successfully"))
                .doOnError(error -> log.error("Failed to retrieve like", error));
    }

    /**
     * Retrieves all likes for a feedback.
     *
     * @param feedback_id the feedback ID
     * @return Flux<LikeResponseDTO>
     */
    public Flux<LikeResponseDTO> getLikesByFeedback(UUID feedback_id) {
        log.info("Retrieving all likes for feedback: {}", feedback_id);

        return validateFeedbackExists(feedback_id)
                .thenMany(like_repository.findByFeedbackId(feedback_id)
                        .flatMap(this::buildResponseWithLikerName)
                )
                .doOnComplete(() -> log.info("All likes retrieved for feedback: {}", feedback_id))
                .doOnError(error -> log.error("Failed to retrieve likes for feedback: {}",
                        feedback_id, error));
    }

    /**
     * Retrieves all likes by a user.
     *
     * @param liker_id the liker ID
     * @return Flux<LikeResponseDTO>
     */
    public Flux<LikeResponseDTO> getLikesByUser(UUID liker_id) {
        log.info("Retrieving all likes by user: {}", liker_id);

        return validateUserExists(liker_id)
                .thenMany(like_repository.findByLikerId(liker_id)
                        .flatMap(this::buildResponseWithLikerName)
                )
                .doOnComplete(() -> log.info("All likes retrieved for user: {}", liker_id))
                .doOnError(error -> log.error("Failed to retrieve likes for user: {}",
                        liker_id, error));
    }

    /**
     * Deletes a like.
     * Only the user who created the like can delete it (verified via JWT token).
     *
     * @param feedback_id the feedback ID
     * @param authorization_header the Authorization header containing JWT token
     * @return Mono<Void>
     */
    @Transactional
    public Mono<Void> deleteLike(UUID feedback_id, String authorization_header) {
        log.info("Deleting like for feedback: {}", feedback_id);

        return extractUserIdFromToken(authorization_header)
                .flatMap(liker_id -> {
                    log.info("Deleting like from user: {}", liker_id);

                    return like_repository.findByFeedbackIdAndLikerId(feedback_id, liker_id)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException(LIKE_NOT_FOUND_MESSAGE)))
                            .flatMap(like ->
                                    like_repository.deleteByFeedbackIdAndLikerId(feedback_id, liker_id)
                                            .then(decrementFeedbackLikeCount(feedback_id))
                            );
                })
                .doOnSuccess(v -> log.info("Like deleted successfully for feedback: {}", feedback_id))
                .doOnError(error -> log.error("Failed to delete like for feedback: {}",
                        feedback_id, error));
    }

    private Mono<UUID> extractUserIdFromToken(String authorization_header) {
        if (authorization_header == null || !authorization_header.startsWith(BEARER_PREFIX)) {
            return Mono.error(new IllegalArgumentException("Invalid authorization header"));
        }

        String token = authorization_header.substring(BEARER_PREFIX.length());

        return jwt_service.validateTokenAndExtractUserId(token)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(USER_NOT_FOUND_MESSAGE)));
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

    private Mono<Void> checkLikeDoesNotExist(UUID feedback_id, UUID liker_id) {
        return like_repository.existsByFeedbackIdAndLikerId(feedback_id, liker_id)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new IllegalArgumentException(LIKE_ALREADY_EXISTS_MESSAGE));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Likes> saveLike(CreateLikeRequestDTO request, UUID liker_id) {
        Likes like = likes_mapper.toEntity(request, liker_id);
        return like_repository.save(like);
    }

    private Mono<Void> incrementFeedbackLikeCount(UUID feedback_id) {
        return feedback_repository.incrementLikesCount(feedback_id);
    }

    private Mono<Void> decrementFeedbackLikeCount(UUID feedback_id) {
        return feedback_repository.decrementLikesCount(feedback_id);
    }

    private Mono<LikeResponseDTO> buildResponseWithLikerName(Likes likes) {
        return app_user_repository.findById(likes.getLiker_id())
                .map(user -> {
                    String liker_name = user.getUser_firstname() != null
                            ? user.getUser_firstname() + " " + user.getUser_lastname()
                            : user.getUser_lastname();
                    return likes_mapper.toResponseDTOWithName(likes, liker_name);
                })
                .defaultIfEmpty(likes_mapper.toResponseDTOWithName(likes, "Unknown User"));
    }
}

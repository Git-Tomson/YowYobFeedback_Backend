package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Likes;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Like entity.
 * Provides reactive database operations for likes on feedbacks.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Repository
public interface LikeRepository extends ReactiveCrudRepository<Likes, UUID> {

    @Query("SELECT * FROM likes WHERE feedback_id = :feedback_id AND liker_id = :liker_id")
    Mono<Likes> findByFeedbackIdAndLikerId(UUID feedback_id, UUID liker_id);

    @Query("SELECT * FROM likes WHERE feedback_id = :feedback_id ORDER BY likes_date_time DESC")
    Flux<Likes> findByFeedbackId(UUID feedback_id);

    @Query("SELECT * FROM likes WHERE liker_id = :liker_id ORDER BY likes_date_time DESC")
    Flux<Likes> findByLikerId(UUID liker_id);

    @Modifying
    @Query("DELETE FROM likes WHERE feedback_id = :feedback_id AND liker_id = :liker_id")
    Mono<Void> deleteByFeedbackIdAndLikerId(UUID feedback_id, UUID liker_id);

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE feedback_id = :feedback_id AND liker_id = :liker_id)")
    Mono<Boolean> existsByFeedbackIdAndLikerId(UUID feedback_id, UUID liker_id);
}

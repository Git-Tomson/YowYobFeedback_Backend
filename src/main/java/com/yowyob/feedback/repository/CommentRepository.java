package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Comment;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Comment entity.
 * Provides reactive database operations for comments.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Repository
public interface CommentRepository extends ReactiveCrudRepository<Comment, UUID> {

    @Query("SELECT * FROM comments WHERE feedback_id = :feedback_id ORDER BY comments_date_time ASC")
    Flux<Comment> findByFeedbackId(UUID feedback_id);

    @Query("SELECT * FROM comments WHERE commenter_id = :commenter_id ORDER BY comments_date_time DESC")
    Flux<Comment> findByCommenterId(UUID commenter_id);

    @Modifying
    @Query("UPDATE comments SET number_of_likes = number_of_likes + 1 WHERE comments_id = :comments_id")
    Mono<Void> incrementApprovalCount(UUID comments_id);

    @Modifying
    @Query("UPDATE comments SET number_of_likes = number_of_likes - 1 WHERE comments_id = :comments_id AND number_of_likes > 0")
    Mono<Void> decrementApprovalCount(UUID comments_id);
}

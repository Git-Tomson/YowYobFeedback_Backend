package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Comment;
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
}

package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Feedback;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Feedback entity.
 * Provides reactive database operations for feedbacks.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Repository
public interface FeedbackRepository extends ReactiveCrudRepository<Feedback, UUID> {

    @Query("SELECT * FROM feedback WHERE target_project_id = :project_id ORDER BY feedback_date_time DESC")
    Flux<Feedback> findByProjectId(UUID project_id);

    @Query("SELECT * FROM feedback WHERE member_id = :member_id ORDER BY feedback_date_time DESC")
    Flux<Feedback> findByMemberId(UUID member_id);

    @Query("SELECT f.* FROM feedback f " +
            "JOIN member m ON f.member_id = m.member_id " +
            "WHERE m.member_pseudo = :member_pseudo AND f.target_project_id = :project_id " +
            "ORDER BY f.feedback_date_time DESC")
    Flux<Feedback> findByMemberPseudoAndProjectId(String member_pseudo, UUID project_id);

    @Query("SELECT f.* FROM feedback f " +
            "JOIN project p ON f.target_project_id = p.project_id " +
            "WHERE p.creator_id = :user_id " +
            "ORDER BY f.feedback_date_time DESC")
    Flux<Feedback> findByUserProjects(UUID user_id);

    @Modifying
    @Query("UPDATE feedback SET number_of_comments = number_of_comments + 1 WHERE feedback_id = :feedback_id")
    Mono<Void> incrementCommentCount(UUID feedback_id);

    @Modifying
    @Query("UPDATE feedback SET number_of_comments = number_of_comments - 1 WHERE feedback_id = :feedback_id AND number_of_comments > 0")
    Mono<Void> decrementCommentCount(UUID feedback_id);
}

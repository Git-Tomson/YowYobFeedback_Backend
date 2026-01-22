package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Approval;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Approval entity.
 * Provides reactive database operations for approvals on comments.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Repository
public interface ApprovalRepository extends ReactiveCrudRepository<Approval, UUID> {

    @Query("SELECT * FROM approval WHERE comments_id = :comments_id AND approver_id = :approver_id")
    Mono<Approval> findByCommentsIdAndApproverId(UUID comments_id, UUID approver_id);

    @Query("SELECT * FROM approval WHERE comments_id = :comments_id ORDER BY approval_date_time DESC")
    Flux<Approval> findByCommentsId(UUID comments_id);

    @Query("SELECT * FROM approval WHERE approver_id = :approver_id ORDER BY approval_date_time DESC")
    Flux<Approval> findByApproverId(UUID approver_id);

    @Modifying
    @Query("DELETE FROM approval WHERE comments_id = :comments_id AND approver_id = :approver_id")
    Mono<Void> deleteByCommentsIdAndApproverId(UUID comments_id, UUID approver_id);

    @Query("SELECT EXISTS(SELECT 1 FROM approval WHERE comments_id = :comments_id AND approver_id = :approver_id)")
    Mono<Boolean> existsByCommentsIdAndApproverId(UUID comments_id, UUID approver_id);
}

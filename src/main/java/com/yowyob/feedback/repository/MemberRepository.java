package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Member;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Member entity operations.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Repository
public interface MemberRepository extends R2dbcRepository<Member, UUID> {

    /**
     * Finds all members of a specific project.
     *
     * @param project_id the project ID
     * @return Flux of members
     */
    @Query("SELECT * FROM member WHERE project_id = :project_id")
    Flux<Member> findByProjectId(UUID project_id);

    /**
     * Finds a member by user ID and project ID.
     *
     * @param user_id the user ID
     * @param project_id the project ID
     * @return Mono of Member
     */
    @Query("SELECT * FROM member WHERE user_id = :user_id AND project_id = :project_id")
    Mono<Member> findByUserIdAndProjectId(UUID user_id, UUID project_id);

    /**
     * Checks if a user is already a member of a project.
     *
     * @param user_id the user ID
     * @param project_id the project ID
     * @return Mono of Boolean
     */
    @Query("SELECT COUNT(*) > 0 FROM member WHERE user_id = :user_id AND project_id = :project_id")
    Mono<Boolean> existsByUserIdAndProjectId(UUID user_id, UUID project_id);

    /**
     * Checks if a pseudo is already used in a project.
     *
     * @param member_pseudo the member pseudo
     * @param project_id the project ID
     * @return Mono of Boolean
     */
    @Query("SELECT COUNT(*) > 0 FROM member WHERE member_pseudo = :member_pseudo AND project_id = :project_id")
    Mono<Boolean> existsByMemberPseudoAndProjectId(String member_pseudo, UUID project_id);

    /**
     * Counts the number of members in a project.
     *
     * @param project_id the project ID
     * @return Mono of Long
     */
    @Query("SELECT COUNT(*) FROM member WHERE project_id = :project_id")
    Mono<Long> countByProjectId(UUID project_id);

    /**
     * Deletes a member by user ID and project ID.
     *
     * @param user_id the user ID
     * @param project_id the project ID
     * @return Mono of Void
     */
    @Query("DELETE FROM member WHERE user_id = :user_id AND project_id = :project_id")
    Mono<Void> deleteByUserIdAndProjectId(UUID user_id, UUID project_id);

    /**
     * Find a member by Project ID and member pseudo
     *
     * @param member_pseudo the member pseudo
     * @param project_id the project id
     * @return Mono of Member
     */
    @Query("SELECT * FROM member WHERE member_pseudo = :member_pseudo AND project_id = :project_id")
    Mono<Member>findByMemberPseudoAndProjectId(String member_pseudo, UUID project_id);
}

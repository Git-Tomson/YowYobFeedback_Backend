package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Project;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Project entity operations.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Repository
public interface ProjectRepository extends R2dbcRepository<Project, UUID> {

    /**
     * Finds all projects created by a specific user.
     *
     * @param creator_id the creator's user ID
     * @return Flux of projects
     */
    @Query("SELECT * FROM project WHERE creator_id = :creator_id")
    Flux<Project> findByCreatorId(UUID creator_id);

    /**
     * Finds all projects where a user is a member, but NOT the creator.
     *
     * @param user_id the user ID
     * @return Flux of projects
     */
    @Query("SELECT p.* FROM project p " +
            "INNER JOIN member m ON p.project_id = m.project_id " +
            "WHERE m.user_id = :user_id " +
            "AND p.creator_id <> :user_id")
    Flux<Project> findProjectsByMemberUserId(@Param("user_id") UUID user_id);
    /**
     * Checks if a project exists with the given ID and code.
     *
     * @param project_id the project ID
     * @param code the project code
     * @return Mono of Boolean
     */
    @Query("SELECT COUNT(*) > 0 FROM project WHERE project_id = :project_id AND code = :code")
    Mono<Boolean> existsByProjectIdAndCode(UUID project_id, String code);

    /**
     * Checks if a project name already exists for a specific creator.
     *
     * @param project_name the project name
     * @param creator_id the creator's user ID
     * @return Mono of Boolean
     */
    @Query("SELECT COUNT(*) > 0 FROM project WHERE project_name = :project_name AND creator_id = :creator_id")
    Mono<Boolean> existsByProjectNameAndCreatorId(String project_name, UUID creator_id);

    /**
     * Finds a project by name and creator ID.
     *
     * @param project_name the project name
     * @param creator_id the creator's user ID
     * @return Mono of Project
     */
    @Query("SELECT * FROM project WHERE project_name = :project_name AND creator_id = :creator_id")
    Mono<Project> findByProjectNameAndCreatorId(String project_name, UUID creator_id);

    /**
     * Deletes a project by name and creator ID.
     *
     * @param project_name the project name
     * @param creator_id the creator's user ID
     * @return Mono of Void
     */
    @Query("DELETE FROM project WHERE project_name = :project_name AND creator_id = :creator_id")
    Mono<Void> deleteByProjectNameAndCreatorId(String project_name, UUID creator_id);
}

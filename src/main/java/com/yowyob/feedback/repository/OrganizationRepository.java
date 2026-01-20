package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Organization;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Organization entity.
 * Provides reactive database operations for organization-specific data.
 *
 * @author Thomas Djotio Ndié
 * @version 1.0
 * @since 2024-12-12
 */
@Repository
public interface OrganizationRepository extends R2dbcRepository<Organization, UUID> {

    /**
     * Finds an organization by their org_id (which is also the user_id).
     *
     * @param org_id the organization identifier
     * @return Mono containing the organization if found, empty Mono otherwise
     */
    @Query("SELECT * FROM organization WHERE org_id = :id")
    Mono<Organization> findByOrgId(UUID org_id);
}

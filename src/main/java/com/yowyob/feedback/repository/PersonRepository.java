package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.Person;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for Person entity.
 * Provides reactive database operations for person-specific data.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 1.0
 */
@Repository
public interface PersonRepository extends R2dbcRepository<Person, UUID> {

    /**
     * Finds a person by their person_id (which is also the user_id).
     *
     * @param person_id the person identifier
     * @return Mono containing the person if found, empty Mono otherwise
     */
    @Query("SELECT * FROM person WHERE person_id = :id")
    Mono<Person> findByPersonId(UUID person_id);
}

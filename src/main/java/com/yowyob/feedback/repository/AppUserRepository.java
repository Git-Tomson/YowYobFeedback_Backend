package com.yowyob.feedback.repository;

import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.UserType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repository interface for AppUser entity.
 * Provides reactive database operations for user management.
 * <p>
 * Uses Spring Data R2DBC for non-blocking database access.
 *
 * @author Thomas Djotio Ndié
 * @version 1.0
 * @since 2024-12-12
 */

@Repository
public interface AppUserRepository extends R2dbcRepository<AppUser, UUID> {

    /**
     * Finds a user by email address.
     *
     * @param email the email to search for
     * @return Mono containing the user if found, empty Mono otherwise
     */
    Mono<AppUser> findByEmail(String email);

    /**
     * Finds a user by contact number.
     *
     * @param contact the contact number to search for
     * @return Mono containing the user if found, empty Mono otherwise
     */
    Mono<AppUser> findByContact(String contact);

    /**
     * Finds a user by either email or contact number.
     * Used for authentication where identifier can be either email or contact.
     *
     * @param identifier the email or contact to search for
     * @return Mono containing the user if found, empty Mono otherwise
     */
    @Query("SELECT * FROM app_user WHERE email = :identifier OR contact = :identifier LIMIT 1")
    Mono<AppUser> findByEmailOrContact(String identifier);

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check
     * @return Mono<Boolean> true if exists, false otherwise
     */
    Mono<Boolean> existsByEmail(String email);

    /**
     * Checks if a user with the given contact exists.
     *
     * @param contact the contact to check
     * @return Mono<Boolean> true if exists, false otherwise
     */
    Mono<Boolean> existsByContact(String contact);

    /**
     * Find all users.
     *
     * @return Flux<AppUser> all users
     */
    Flux<AppUser> findAll();

    /**
     * Find all users by type.
     *
     * @param userType the user type (PERSON or ORGANIZATION)
     * @return Flux<AppUser> users of the specified type
     */
    @Query("SELECT * FROM app_user WHERE user_type = :userType")
    Flux<AppUser> findAllByUserType(UserType userType);
}

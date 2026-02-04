package com.yowyob.feedback.service;

import com.yowyob.feedback.dto.response.UserResponseDTO;
import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.UserType;
import com.yowyob.feedback.mapper.UserMapper;
import com.yowyob.feedback.repository.AppUserRepository;
import com.yowyob.feedback.repository.OrganizationRepository;
import com.yowyob.feedback.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service for user management operations.
 * Handles retrieval and filtering of user data.
 *
 * @author Thomas Djotio Ndié
 * @since 2026-02-04
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PersonRepository personRepository;
    private final OrganizationRepository organizationRepository;
    private final UserMapper userMapper;

    /**
     * Retrieves all users in the system.
     *
     * @return Flux<UserResponseDTO> all users with complete information
     */
    @Transactional(readOnly = true)
    public Flux<UserResponseDTO> getAllUsers() {
        log.info("Fetching all users");

        return appUserRepository.findAll()
                .flatMap(this::enrichUserResponse)
                .doOnComplete(() -> log.info("All users retrieved successfully"))
                .doOnError(error -> log.error("Failed to retrieve all users: {}", error.getMessage()));
    }

    /**
     * Retrieves all users of type PERSON.
     *
     * @return Flux<UserResponseDTO> all person-type users with occupation
     */
    @Transactional(readOnly = true)
    public Flux<UserResponseDTO> getAllPersons() {
        log.info("Fetching all PERSON users");

        return appUserRepository.findAllByUserType(UserType.PERSON)
                .flatMap(this::enrichUserResponse)
                .doOnComplete(() -> log.info("All PERSON users retrieved successfully"))
                .doOnError(error -> log.error("Failed to retrieve PERSON users: {}", error.getMessage()));
    }

    /**
     * Retrieves all users of type ORGANIZATION.
     *
     * @return Flux<UserResponseDTO> all organization-type users with location
     */
    @Transactional(readOnly = true)
    public Flux<UserResponseDTO> getAllOrganizations() {
        log.info("Fetching all ORGANIZATION users");

        return appUserRepository.findAllByUserType(UserType.ORGANIZATION)
                .flatMap(this::enrichUserResponse)
                .doOnComplete(() -> log.info("All ORGANIZATION users retrieved successfully"))
                .doOnError(error -> log.error("Failed to retrieve ORGANIZATION users: {}", error.getMessage()));
    }

    /**
     * Enriches user response with type-specific data.
     * Adds occupation for PERSON or location for ORGANIZATION.
     *
     * @param appUser the base user entity
     * @return Mono<UserResponseDTO> enriched user response
     */
    private Mono<UserResponseDTO> enrichUserResponse(AppUser appUser) {
        UserResponseDTO baseResponse = userMapper.toUserResponseDTO(appUser);

        if (appUser.getUser_type() == UserType.PERSON) {
            return personRepository.findByPersonId(appUser.getUser_id())
                    .map(person -> baseResponse.toBuilder()
                            .occupation(person.getOccupation())
                            .build())
                    .defaultIfEmpty(baseResponse);
        } else if (appUser.getUser_type() == UserType.ORGANIZATION) {
            return organizationRepository.findByOrgId(appUser.getUser_id())
                    .map(organization -> baseResponse.toBuilder()
                            .location(organization.getLocation())
                            .build())
                    .defaultIfEmpty(baseResponse);
        }

        return Mono.just(baseResponse);
    }
}

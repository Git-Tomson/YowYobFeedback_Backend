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

import java.util.UUID;

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
     * Retrieves all users in the system except the current user.
     *
     * @param currentUserId the ID of the current authenticated user to exclude
     * @return Flux<UserResponseDTO> all users except the current user with complete information
     */
    @Transactional(readOnly = true)
    public Flux<UserResponseDTO> getAllUsers(UUID currentUserId) {
        log.info("Fetching all users except user: {}", currentUserId);

        return appUserRepository.findAllExcludingUser(currentUserId)
                .flatMap(this::enrichUserResponse)
                .doOnComplete(() -> log.info("All users retrieved successfully (excluding current user)"))
                .doOnError(error -> log.error("Failed to retrieve all users: {}", error.getMessage()));
    }

    /**
     * Retrieves all users of type PERSON except the current user.
     *
     * @param currentUserId the ID of the current authenticated user to exclude
     * @return Flux<UserResponseDTO> all person-type users except the current user with occupation
     */
    @Transactional(readOnly = true)
    public Flux<UserResponseDTO> getAllPersons(UUID currentUserId) {
        log.info("Fetching all PERSON users except user: {}", currentUserId);

        return appUserRepository.findAllByUserTypeExcludingUser(UserType.PERSON, currentUserId)
                .flatMap(this::enrichUserResponse)
                .doOnComplete(() -> log.info("All PERSON users retrieved successfully (excluding current user)"))
                .doOnError(error -> log.error("Failed to retrieve PERSON users: {}", error.getMessage()));
    }

    /**
     * Retrieves all users of type ORGANIZATION except the current user.
     *
     * @param currentUserId the ID of the current authenticated user to exclude
     * @return Flux<UserResponseDTO> all organization-type users except the current user with location
     */
    @Transactional(readOnly = true)
    public Flux<UserResponseDTO> getAllOrganizations(UUID currentUserId) {
        log.info("Fetching all ORGANIZATION users except user: {}", currentUserId);

        return appUserRepository.findAllByUserTypeExcludingUser(UserType.ORGANIZATION, currentUserId)
                .flatMap(this::enrichUserResponse)
                .doOnComplete(() -> log.info("All ORGANIZATION users retrieved successfully (excluding current user)"))
                .doOnError(error -> log.error("Failed to retrieve ORGANIZATION users: {}", error.getMessage()));
    }
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

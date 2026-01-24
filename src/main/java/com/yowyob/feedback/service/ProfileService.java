package com.yowyob.feedback.service;

import com.yowyob.feedback.constant.AppConstants;
import com.yowyob.feedback.dto.request.UpdateProfileRequestDTO;
import com.yowyob.feedback.dto.response.AuthResponseDTO;
import com.yowyob.feedback.dto.response.UserResponseDTO;
import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.Organization;
import com.yowyob.feedback.entity.Person;
import com.yowyob.feedback.entity.UserType;
import com.yowyob.feedback.mapper.UserMapper;
import com.yowyob.feedback.repository.AppUserRepository;
import com.yowyob.feedback.repository.OrganizationRepository;
import com.yowyob.feedback.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import com.yowyob.feedback.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Service class handling user profile operations.
 * Manages profile updates with validation for unique constraints.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-24
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final AppUserRepository app_user_repository;
    private final PersonRepository person_repository;
    private final OrganizationRepository organization_repository;
    private final UserMapper user_mapper;
    private final PasswordEncoder password_encoder;

    /**
     * Updates user profile information for authenticated user.
     * User is identified by user_id.
     * Only provided fields will be updated.
     * Validates unique constraints for email and contact.
     *
     * @param user_id the ID of the authenticated user
     * @param update_request the update data
     * @return Mono<AuthResponseDTO> containing updated user information
     */
    @Transactional
    public Mono<AuthResponseDTO> updateProfile(UUID user_id,
                                               UpdateProfileRequestDTO update_request) {
        log.info("Profile update attempt for user_id: {}", user_id);

        return app_user_repository.findById(user_id)
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException(AppConstants.USER_NOT_FOUND_MESSAGE)))
                .flatMap(existing_user -> validateUniqueConstraints(existing_user, update_request)
                        .then(Mono.just(existing_user)))
                .flatMap(existing_user -> validateEmailContactPresence(existing_user,
                        update_request)
                        .then(Mono.just(existing_user)))
                .flatMap(existing_user -> updateUserFields(existing_user, update_request))
                .flatMap(app_user_repository::save)
                .flatMap(updated_user -> updateTypeSpecificData(updated_user, update_request))
                .flatMap(updated_user -> buildCompleteUserResponse(updated_user))
                .map(user_response -> AuthResponseDTO.builder()
                        .message(AppConstants.PROFILE_UPDATE_SUCCESS_MESSAGE)
                        .user_response_dto(user_response)
                        .build())
                .doOnSuccess(response -> log.info("Profile updated successfully for user_id: {}",
                        user_id))
                .doOnError(error -> log.error("Profile update failed for user_id {}: {}",
                        user_id, error.getMessage()));
    }

    /**
     * Validates unique constraints for email and contact.
     * Checks that new email/contact are not already used by other users.
     *
     * @param existing_user the current user entity
     * @param update_request the update request
     * @return Mono<Void> completes successfully if constraints are valid
     */
    private Mono<Void> validateUniqueConstraints(AppUser existing_user,
                                                 UpdateProfileRequestDTO update_request) {
        Mono<Void> email_validation = Mono.empty();
        Mono<Void> contact_validation = Mono.empty();

        if (update_request.email() != null && !update_request.email().isBlank()) {
            if (!update_request.email().equals(existing_user.getEmail())) {
                email_validation = app_user_repository.existsByEmail(update_request.email())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new IllegalArgumentException(
                                        AppConstants.EMAIL_ALREADY_USED_MESSAGE));
                            }
                            return Mono.empty();
                        });
            }
        }

        if (update_request.contact() != null && !update_request.contact().isBlank()) {
            if (!update_request.contact().equals(existing_user.getContact())) {
                contact_validation = app_user_repository.existsByContact(update_request.contact())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new IllegalArgumentException(
                                        AppConstants.CONTACT_ALREADY_USED_MESSAGE));
                            }
                            return Mono.empty();
                        });
            }
        }

        return email_validation.then(contact_validation);
    }

    /**
     * Validates that at least email or contact remains after update.
     *
     * @param existing_user the current user entity
     * @param update_request the update request
     * @return Mono<Void> completes successfully if validation passes
     */
    private Mono<Void> validateEmailContactPresence(AppUser existing_user,
                                                    UpdateProfileRequestDTO update_request) {
        String final_email = update_request.email() != null ? update_request.email() :
                existing_user.getEmail();
        String final_contact = update_request.contact() != null ? update_request.contact() :
                existing_user.getContact();

        boolean has_email = final_email != null && !final_email.isBlank();
        boolean has_contact = final_contact != null && !final_contact.isBlank();

        if (!has_email && !has_contact) {
            return Mono.error(new IllegalArgumentException(
                    AppConstants.CANNOT_REMOVE_BOTH_EMAIL_AND_CONTACT_MESSAGE));
        }

        return Mono.empty();
    }

    /**
     * Updates user fields with provided values.
     * Only non-null fields from the request are updated.
     *
     * @param existing_user the current user entity
     * @param update_request the update request
     * @return Mono<AppUser> the updated user entity
     */
    private Mono<AppUser> updateUserFields(AppUser existing_user,
                                           UpdateProfileRequestDTO update_request) {
        if (update_request.user_firstname() != null) {
            existing_user.setUser_firstname(update_request.user_firstname());
        }

        if (update_request.user_lastname() != null) {
            existing_user.setUser_lastname(update_request.user_lastname());
        }

        if (update_request.email() != null) {
            existing_user.setEmail(update_request.email());
        }

        if (update_request.contact() != null) {
            existing_user.setContact(update_request.contact());
        }

        if (update_request.user_logo() != null) {
            existing_user.setUser_logo(update_request.user_logo());
        }

        if (update_request.domain() != null) {
            existing_user.setDomain(update_request.domain());
        }

        if (update_request.description() != null) {
            existing_user.setDescription(update_request.description());
        }

        if (update_request.password() != null && !update_request.password().isBlank()) {
            existing_user.setPassword(password_encoder.encode(update_request.password()));
        }

        return Mono.just(existing_user);
    }

    /**
     * Updates type-specific data for person or organization.
     * Verifies entity exists before updating.
     * Throws ResourceNotFoundException if entity not found.
     *
     * @param saved_user the saved app_user entity
     * @param update_request the update request containing type-specific data
     * @return Mono<AppUser> the user entity after subtype update
     * @throws ResourceNotFoundException if person or organization data not found
     */
    private Mono<AppUser> updateTypeSpecificData(AppUser saved_user,
                                                 UpdateProfileRequestDTO update_request) {
        log.debug("Updating type-specific data for user_type: {}", saved_user.getUser_type());

        if (UserType.PERSON.equals(saved_user.getUser_type())) {
            return person_repository.findByPersonId(saved_user.getUser_id())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                            AppConstants.PERSON_DATA_NOT_FOUND_MESSAGE)))
                    .flatMap(existing_person -> {
                        // Update only if new value provided
                        if (update_request.occupation() != null &&
                                !update_request.occupation().isBlank()) {
                            existing_person.setOccupation(update_request.occupation());
                        }
                        existing_person.setNew(false); // Force UPDATE
                        return person_repository.save(existing_person);
                    })
                    .thenReturn(saved_user);
        }

        if (UserType.ORGANIZATION.equals(saved_user.getUser_type())) {
            return organization_repository.findByOrgId(saved_user.getUser_id())
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                            AppConstants.ORGANIZATION_DATA_NOT_FOUND_MESSAGE)))
                    .flatMap(existing_org -> {
                        // Update only if new value provided
                        if (update_request.location() != null &&
                                !update_request.location().isBlank()) {
                            existing_org.setLocation(update_request.location());
                        }
                        existing_org.setNew(false); // Force UPDATE
                        return organization_repository.save(existing_org);
                    })
                    .thenReturn(saved_user);
        }

        return Mono.error(new IllegalStateException("Unknown user type: "
                + saved_user.getUser_type()));
    }

    /**
     * Builds complete user response including type-specific data.
     *
     * @param app_user the user entity
     * @return Mono<UserResponseDTO> complete user information
     */
    private Mono<UserResponseDTO> buildCompleteUserResponse(AppUser app_user) {
        UserResponseDTO base_response = user_mapper.toUserResponseDTO(app_user);

        if (UserType.PERSON.equals(app_user.getUser_type())) {
            return person_repository.findByPersonId(app_user.getUser_id())
                    .map(person -> base_response.toBuilder()
                            .occupation(person.getOccupation())
                            .build());
        } else {
            return organization_repository.findByOrgId(app_user.getUser_id())
                    .map(organization -> base_response.toBuilder()
                            .location(organization.getLocation())
                            .build());
        }
    }
}

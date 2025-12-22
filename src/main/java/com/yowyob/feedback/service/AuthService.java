package com.yowyob.feedback.service;

import com.yowyob.feedback.constant.AppConstants;
import com.yowyob.feedback.dto.request.LoginRequestDTO;
import com.yowyob.feedback.dto.request.RegisterRequestDTO;
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

import java.util.UUID;

/**
 * Service class handling authentication operations.
 * Manages user registration and login business logic.
 *
 * This service handles the inheritance hierarchy where AppUser is the base class
 * and Person/Organization are subtypes. Registration creates entries in both
 * app_user table and the appropriate subtype table.
 *
 * This service uses reactive programming with Project Reactor.
 * All operations return Mono for asynchronous processing.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository app_user_repository;
    private final UserMapper user_mapper;
    private final PasswordEncoder password_encoder;
    private final OrganizationRepository organization_repository;
    private final PersonRepository person_repository;
    private final JwtService jwtService;

    /**
     * Registers a new user in the system.
     *
     * Validation steps:
     * 1. Checks that at least email or contact is provided
     * 2. Validates type-specific required fields (occupation for PERSON, location for ORGANIZATION)
     * 3. Verifies user doesn't already exist
     * 4. Encodes password before storage
     * 5. Saves user to app_user table
     * 6. Saves type-specific data to person or organization table
     *
     * @param register_request the registration data
     * @return Mono<AuthResponseDTO> containing registration result
     * @throws IllegalArgumentException if validation fails
     */
    //Le @Transactionnal garantie l'atomicité
    @Transactional
    public Mono<AuthResponseDTO> register(RegisterRequestDTO register_request) {
        log.info("Register attempt for email: {}, contact: {}, type: {}",
                register_request.email(),
                register_request.contact(),
                register_request.user_type());

        return validateRegistrationData(register_request)
                .then(validateTypeSpecificFields(register_request))
                .then(checkUserDoesNotExist(register_request))
                .then(createAndSaveUser(register_request))
                .flatMap(saved_user -> saveTypeSpecificData(saved_user, register_request))
                .flatMap(saved_user -> buildCompleteUserResponse(saved_user,
                        register_request.user_type()))
                .map(this::buildSuccessfulRegistrationResponse)
                .doOnSuccess(response -> log.info("User registered successfully"))
                .doOnError(error -> log.error("Registration failed: {}", error.getMessage()));
    }

    /**
     * Authenticates a user with credentials.
     *
     * Authentication steps:
     * 1. Finds user by email or contact
     * 2. Verifies password matches
     * 3. Loads type-specific data (occupation or location)
     * 4. Returns complete user information if successful
     *
     * @param login_request the login credentials
     * @return Mono<AuthResponseDTO> containing login result
     * @throws IllegalArgumentException if authentication fails
     */
    @Transactional
    public Mono<AuthResponseDTO> login(LoginRequestDTO login_request) {
        log.info("Login attempt for identifier: {}", login_request.identifier());

        return app_user_repository.findByEmailOrContact(login_request.identifier())
                .switchIfEmpty(Mono.error(
                        new IllegalArgumentException(AppConstants.USER_NOT_FOUND_MESSAGE)))
                .flatMap(user -> validatePasswordAndBuildResponse(user,
                        login_request.password()))
                .doOnSuccess(response -> log.info("Login successful"))
                .doOnError(error -> log.error("Login failed: {}", error.getMessage()));
    }


    /**
     * Validates that either email or contact is provided.
     *
     * @param register_request the registration data to validate
     * @return Mono<Void> completes successfully if valid
     * @throws IllegalArgumentException if validation fails
     */
    private Mono<Void> validateRegistrationData(RegisterRequestDTO register_request) {
        boolean has_email = register_request.email() != null &&
                !register_request.email().isBlank();
        boolean has_contact = register_request.contact() != null &&
                !register_request.contact().isBlank();

        if (!has_email && !has_contact) {
            return Mono.error(new IllegalArgumentException(
                    AppConstants.EMAIL_OR_CONTACT_REQUIRED_MESSAGE));
        }

        return Mono.empty();
    }

    private Mono<Void> validateTypeSpecificFields(RegisterRequestDTO register_request) {
        if (UserType.PERSON.equals(register_request.user_type())) {
            if (register_request.occupation() == null ||
                    register_request.occupation().isBlank()) {
                return Mono.error(new IllegalArgumentException(
                        AppConstants.OCCUPATION_REQUIRED_FOR_PERSON_MESSAGE));
            }
        } else if (UserType.ORGANIZATION.equals(register_request.user_type())) {
            if (register_request.location() == null ||
                    register_request.location().isBlank()) {
                return Mono.error(new IllegalArgumentException(
                        AppConstants.LOCATION_REQUIRED_FOR_ORGANIZATION_MESSAGE));
            }
        }
        return Mono.empty();
    }

    /**
     * Checks if user already exists with given email or contact.
     *
     * @param register_request the registration data to check
     * @return Mono<Void> completes successfully if user doesn't exist
     * @throws IllegalArgumentException if user already exists
     */
    private Mono<Void> checkUserDoesNotExist(RegisterRequestDTO register_request) {
        Mono<Boolean> email_exists = register_request.email() != null &&
                !register_request.email().isBlank()
                ? app_user_repository.existsByEmail(register_request.email())
                : Mono.just(false);

        Mono<Boolean> contact_exists = register_request.contact() != null &&
                !register_request.contact().isBlank()
                ? app_user_repository.existsByContact(register_request.contact())
                : Mono.just(false);

        return Mono.zip(email_exists, contact_exists)
                .flatMap(tuple -> {
                    if (tuple.getT1() || tuple.getT2()) {
                        return Mono.error(new IllegalArgumentException(
                                AppConstants.USER_ALREADY_EXISTS_MESSAGE));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Creates user entity from DTO and saves to app_user table.
     * Password is encoded before saving.
     *
     * @param register_request the registration data
     * @return Mono<AppUser> the saved user entity
     */
    private Mono<AppUser> createAndSaveUser(RegisterRequestDTO register_request) {
        AppUser user = user_mapper.toUserEntity(register_request);
        user.setPassword(password_encoder.encode(register_request.password()));

        return app_user_repository.save(user);
    }

    /**
     * Saves type-specific data to person or organization table.
     * The ID used is the same as the user_id from app_user.
     *
     * @param saved_user the saved app_user entity
     * @param register_request the registration request containing type-specific data
     * @return Mono<AppUser> the user entity after subtype save
     */
    private Mono<AppUser> saveTypeSpecificData(AppUser saved_user,
                                               RegisterRequestDTO register_request) {
        log.info("Sauvegarde spécifique pour le type : {}", saved_user.getUser_type());

        if (UserType.PERSON.equals(saved_user.getUser_type())) {
            Person person = user_mapper.toPersonEntity(register_request);
            person.setPerson_id(saved_user.getUser_id());
            person.setNew(true);
            return person_repository.save(person)
                    .map(p -> saved_user); // Utiliser map ou thenReturn
        }

        if (UserType.ORGANIZATION.equals(saved_user.getUser_type())) {
            Organization organization = user_mapper.toOrganizationEntity(register_request);
            organization.setOrg_id(saved_user.getUser_id());
            organization.setNew(true);
            return organization_repository.save(organization)
                    .thenReturn(saved_user);
        }

        // SI ON ARRIVE ICI, C'EST UNE ERREUR -> ROLLBACK
        return Mono.error(new IllegalStateException("Type d'utilisateur inconnu : "
                + saved_user.getUser_type()));
    }

    /**
     * Builds complete user response including type-specific data.
     *
     * @param app_user the base user entity
     * @param user_type the type of user (PERSON or ORGANIZATION)
     * @return Mono<UserResponseDTO> complete user information
     */
    private Mono<UserResponseDTO> buildCompleteUserResponse(AppUser app_user,
                                                            UserType user_type) {
        UserResponseDTO base_response = user_mapper.toUserResponseDTO(app_user);

        if (user_type == UserType.PERSON) {
            return person_repository.findByPersonId(app_user.getUser_id())
                    .map(person -> {
                        return base_response.toBuilder()
                                .occupation(person.getOccupation())
                                .build();
                    });
        } else {
            return organization_repository.findByOrgId(app_user.getUser_id())
                    .map(organization -> {
                        return base_response.toBuilder()
                                .location(organization.getLocation())
                                .build();
                    });
        }
    }

    /**
     * Builds successful registration response.
     *
     * @param user_response the complete user response DTO
     * @return AuthResponseDTO with user data
     */
    private AuthResponseDTO buildSuccessfulRegistrationResponse(
            UserResponseDTO user_response) {
        return AuthResponseDTO.builder()
                .message(AppConstants.REGISTRATION_SUCCESS_MESSAGE)
                .user_response_dto(user_response)
                .token(AppConstants.TOKEN_SIGNATURE + UUID.randomUUID().toString())
                .build();
    }

    /**
     * Validates password and builds complete login response with type-specific data.
     *
     * @param user the user entity
     * @param provided_password the password to validate
     * @return Mono<AuthResponseDTO> login response if password is valid
     * @throws IllegalArgumentException if password is invalid
     */
    private Mono<AuthResponseDTO> validatePasswordAndBuildResponse(AppUser user,
                                                                   String provided_password) {
        if (!password_encoder.matches(provided_password, user.getPassword())) {
            return Mono.error(new IllegalArgumentException(
                    AppConstants.INVALID_PASSWORD_MESSAGE));
        }

        return buildCompleteUserResponse(user, user.getUser_type())
                .map(user_response -> AuthResponseDTO.builder()
                        .message(AppConstants.LOGIN_SUCCESS_MESSAGE)
                        .user_response_dto(user_response)
                        .token(jwtService.generateToken(user))
                        .build());
    }

}

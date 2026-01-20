package com.yowyob.feedback.service;

import com.yowyob.feedback.constant.AppConstants;
import com.yowyob.feedback.dto.request.LoginRequestDTO;
import com.yowyob.feedback.dto.request.RegisterRequestDTO;
import com.yowyob.feedback.dto.response.AuthResponseDTO;
import com.yowyob.feedback.dto.response.TwoFactorSetupResponseDTO;
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

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository app_user_repository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final PersonRepository personRepository;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;

    /**
     * Registers a new user and returns a valid JWT token immediately.
     */
    @Transactional
    public Mono<AuthResponseDTO> register(RegisterRequestDTO registerRequest) {
        log.info("Register attempt for email: {}, contact: {}, type: {}",
                registerRequest.email(), registerRequest.contact(), registerRequest.user_type());

        return validateRegistrationData(registerRequest)
                .then(validateTypeSpecificFields(registerRequest))
                .then(Mono.defer(()->checkUserDoesNotExist(registerRequest)))
                .then(Mono.defer(()->createAndSaveUser(registerRequest)))
                .flatMap(savedUser -> saveTypeSpecificData(savedUser, registerRequest))
                // Ici, on récupère l'utilisateur complet pour la réponse
                .flatMap(savedUser -> buildCompleteUserResponse(savedUser, registerRequest.user_type())
                        // On génère le token sur l'utilisateur FRAÎCHEMENT créé
                        .map(userResponse -> buildAuthResponse(userResponse, savedUser, AppConstants.REGISTRATION_SUCCESS_MESSAGE))
                )
                .doOnSuccess(response -> log.info("User registered successfully"))
                .doOnError(error -> log.error("Registration failed: {}", error.getMessage()));
    }

    /**
     * Authenticates a user.
     */
    @Transactional
    public Mono<AuthResponseDTO> login(LoginRequestDTO loginRequest) {
        log.info("Login attempt for identifier: {}", loginRequest.identifier());

        return app_user_repository.findByEmailOrContact(loginRequest.identifier())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(AppConstants.USER_NOT_FOUND_MESSAGE)))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
                        return Mono.error(new IllegalArgumentException(AppConstants.INVALID_PASSWORD_MESSAGE));
                    }
                    // Si le mot de passe est bon, on construit la réponse
                    return buildCompleteUserResponse(user, user.getUser_type())
                            .map(userResponse -> buildAuthResponse(userResponse, user, AppConstants.LOGIN_SUCCESS_MESSAGE));
                })
                .doOnSuccess(response -> log.info("Login successful"))
                .doOnError(error -> log.error("Login failed: {}", error.getMessage()));
    }

    // -------------------------------------------------------------------------
    // PRIVATE HELPER METHODS
    // -------------------------------------------------------------------------

    private Mono<Void> validateRegistrationData(RegisterRequestDTO request) {
        boolean hasEmail = request.email() != null && !request.email().isBlank();
        boolean hasContact = request.contact() != null && !request.contact().isBlank();

        if (!hasEmail && !hasContact) {
            return Mono.error(new IllegalArgumentException(AppConstants.EMAIL_OR_CONTACT_REQUIRED_MESSAGE));
        }
        return Mono.empty();
    }

    private Mono<Void> validateTypeSpecificFields(RegisterRequestDTO request) {
        if (UserType.PERSON.equals(request.user_type())) {
            if (request.occupation() == null || request.occupation().isBlank()) {
                return Mono.error(new IllegalArgumentException(AppConstants.OCCUPATION_REQUIRED_FOR_PERSON_MESSAGE));
            }
        } else if (UserType.ORGANIZATION.equals(request.user_type())) {
            if (request.location() == null || request.location().isBlank()) {
                return Mono.error(new IllegalArgumentException(AppConstants.LOCATION_REQUIRED_FOR_ORGANIZATION_MESSAGE));
            }
        }
        return Mono.empty();
    }

    private Mono<Void> checkUserDoesNotExist(RegisterRequestDTO request) {
        // Utilisation de Mono.defer pour éviter d'exécuter la requête si l'email/contact est null
        Mono<Boolean> emailExists = (request.email() != null && !request.email().isBlank())
                ? app_user_repository.existsByEmail(request.email())
                : Mono.just(false);

        Mono<Boolean> contactExists = (request.contact() != null && !request.contact().isBlank())
                ? app_user_repository.existsByContact(request.contact())
                : Mono.just(false);

        return Mono.zip(emailExists, contactExists)
                .flatMap(tuple -> {
                    boolean exists = tuple.getT1() || tuple.getT2();
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(AppConstants.USER_ALREADY_EXISTS_MESSAGE));
                    }
                    return Mono.empty();
                });
    }

    private Mono<AppUser> createAndSaveUser(RegisterRequestDTO request) {
        AppUser user = userMapper.toUserEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        return app_user_repository.save(user);
    }

    private Mono<AppUser> saveTypeSpecificData(AppUser savedUser, RegisterRequestDTO request) {
        // On vérifie le type et on sauvegarde dans la table enfant correspondante
        if (UserType.PERSON.equals(savedUser.getUser_type())) {
            Person person = userMapper.toPersonEntity(request);
            person.setPerson_id(savedUser.getUser_id());
            person.setNew(true); // Crucial pour R2DBC (force INSERT au lieu d'UPDATE)
            return personRepository.save(person).thenReturn(savedUser);
        }

        if (UserType.ORGANIZATION.equals(savedUser.getUser_type())) {
            Organization organization = userMapper.toOrganizationEntity(request);
            organization.setOrg_id(savedUser.getUser_id());
            organization.setNew(true); // Crucial pour R2DBC
            return organizationRepository.save(organization).thenReturn(savedUser);
        }

        return Mono.error(new IllegalStateException("Unknown user type: " + savedUser.getUser_type()));
    }

    private Mono<UserResponseDTO> buildCompleteUserResponse(AppUser appUser, UserType userType) {
        UserResponseDTO baseResponse = userMapper.toUserResponseDTO(appUser);

        if (userType == UserType.PERSON) {
            return personRepository.findByPersonId(appUser.getUser_id())
                    .map(person -> baseResponse.toBuilder()
                            .occupation(person.getOccupation())
                            .build())
                    // Fallback si jamais la donnée n'est pas trouvée (ne devrait pas arriver en transaction)
                    .defaultIfEmpty(baseResponse);
        } else {
            return organizationRepository.findByOrgId(appUser.getUser_id())
                    .map(organization -> baseResponse.toBuilder()
                            .location(organization.getLocation())
                            .build())
                    .defaultIfEmpty(baseResponse);
        }
    }

    /**
     * Méthode générique pour construire la réponse finale (Login ou Register)
     */
    private AuthResponseDTO buildAuthResponse(UserResponseDTO userResponse, AppUser userEntity, String message) {
        return AuthResponseDTO.builder()
                .message(message)
                .user_response_dto(userResponse)
                .token(jwtService.generateToken(userEntity)) // Utilisation du vrai service JWT
                .build();
    }

    /**
     * Enables two-factor authentication for a user.
     *
     * @param user_id the user ID
     * @return Mono<TwoFactorSetupResponseDTO> containing QR code and backup codes
     */
    @Transactional
    public Mono<TwoFactorSetupResponseDTO> enableTwoFactor(UUID user_id) {
        log.info("Enabling 2FA for user: {}", user_id);

        return app_user_repository.findById(user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(AppConstants.USER_NOT_FOUND_MESSAGE)))
                .flatMap(user -> {
                    try {
                        String secret = twoFactorService.generateSecret();
                        String qr_code_url = twoFactorService.generateQrCodeUrl(secret, user.getEmail());
                        String[] backup_codes = twoFactorService.generateBackupCodes();

                        user.setTwo_fa_enabled(true);
                        user.setTwo_fa_secret(secret);
                        user.setTwo_fa_backup_codes(backup_codes);

                        return app_user_repository.save(user)
                                .thenReturn(TwoFactorSetupResponseDTO.builder()
                                        .secret(secret)
                                        .qr_code_url(qr_code_url)
                                        .backup_codes(backup_codes)
                                        .build());
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to setup 2FA: " + e.getMessage()));
                    }
                })
                .doOnSuccess(response -> log.info("2FA enabled successfully"))
                .doOnError(error -> log.error("Failed to enable 2FA: {}", error.getMessage()));
    }

    /**
     * Disables two-factor authentication for a user.
     *
     * @param user_id the user ID
     * @return Mono<String> success message
     */
    @Transactional
    public Mono<String> disableTwoFactor(UUID user_id) {
        log.info("Disabling 2FA for user: {}", user_id);

        return app_user_repository.findById(user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(AppConstants.USER_NOT_FOUND_MESSAGE)))
                .flatMap(user -> {
                    user.setTwo_fa_enabled(false);
                    user.setTwo_fa_secret(null);
                    user.setTwo_fa_backup_codes(null);

                    return app_user_repository.save(user)
                            .thenReturn(AppConstants.TWO_FA_DISABLED_SUCCESS);
                })
                .doOnSuccess(msg -> log.info("2FA disabled successfully"))
                .doOnError(error -> log.error("Failed to disable 2FA: {}", error.getMessage()));
    }

    /**
     * Verifies two-factor authentication code.
     *
     * @param identifier user email or contact
     * @param code 2FA code
     * @return Mono<AuthResponseDTO> authentication response if successful
     */
    @Transactional
    public Mono<AuthResponseDTO> verifyTwoFactorCode(String identifier, String code) {
        log.info("2FA verification attempt for: {}", identifier);

        return app_user_repository.findByEmailOrContact(identifier)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(AppConstants.USER_NOT_FOUND_MESSAGE)))
                .flatMap(user -> {
                    if (!Boolean.TRUE.equals(user.getTwo_fa_enabled())) {
                        return Mono.error(new IllegalArgumentException(AppConstants.TWO_FA_NOT_ENABLED));
                    }

                    boolean is_valid = twoFactorService.verifyCode(user.getTwo_fa_secret(), code);
                    boolean is_backup_code = false;

                    if (!is_valid && user.getTwo_fa_backup_codes() != null) {
                        is_backup_code = twoFactorService.verifyBackupCode(user.getTwo_fa_backup_codes(), code);
                        if (is_backup_code) {
                            String[] updated_codes = twoFactorService.removeBackupCode(
                                    user.getTwo_fa_backup_codes(), code);
                            user.setTwo_fa_backup_codes(updated_codes);
                            return app_user_repository.save(user)
                                    .then(buildAuthResponse(user));
                        }
                    }

                    if (!is_valid && !is_backup_code) {
                        return Mono.error(new IllegalArgumentException(AppConstants.INVALID_TWO_FA_CODE));
                    }

                    return buildAuthResponse(user);
                })
                .doOnSuccess(response -> log.info("2FA verification successful"))
                .doOnError(error -> log.error("2FA verification failed: {}", error.getMessage()));
    }

    /**
     * Builds authentication response with user data and token.
     *
     * @param user the authenticated user
     * @return Mono<AuthResponseDTO>
     */
    private Mono<AuthResponseDTO> buildAuthResponse(AppUser user) {
        return buildCompleteUserResponse(user, user.getUser_type())
                .map(user_response -> AuthResponseDTO.builder()
                        .message(AppConstants.LOGIN_SUCCESS_MESSAGE)
                        .user_response_dto(user_response)
                        .token(jwtService.generateToken(user))
                        .build());
    }

    /**
     * Gets current user information by identifier.
     *
     * @param identifier email or contact
     * @return Mono<UserResponseDTO> current user data
     */
    @Transactional(readOnly = true)
    public Mono<UserResponseDTO> getCurrentUser(String identifier) {
        log.info("Fetching current user information for: {}", identifier);

        // On cherche par Email, Contact OU par l'ID (UUID)
        return app_user_repository.findByEmailOrContact(identifier)
                // Si non trouvé par email/contact, on essaie par ID
                .switchIfEmpty(Mono.defer(() -> {
                    try {
                        UUID user_id = UUID.fromString(identifier);
                        return app_user_repository.findById(user_id);
                    } catch (IllegalArgumentException e) {
                        return Mono.empty();
                    }
                }))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(AppConstants.USER_NOT_FOUND_MESSAGE)))
                .flatMap(user -> buildCompleteUserResponse(user, user.getUser_type()))
                .doOnSuccess(response -> log.info("User information retrieved successfully"))
                .doOnError(error -> log.error("Failed to retrieve user information: {}", error.getMessage()));
    }
    /**
     * Logs out the current user.
     * In JWT authentication, this is mainly symbolic as token invalidation happens client-side.
     *
     * @return Mono<Map<String, String>> logout message
     */
    public Mono<Map<String, String>> logout() {
        log.info("User logout processed");
        return Mono.just(Map.of(
                "message", "Logout successful. Please discard your authentication token."
        ));
    }

    /**
     * Gets user ID by identifier (email or contact).
     *
     * @param identifier email or contact
     * @return Mono<UUID> user ID
     */
    public Mono<UUID> getUserIdByIdentifier(String identifier) {
        return app_user_repository.findByEmailOrContact(identifier)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(AppConstants.USER_NOT_FOUND_MESSAGE)))
                .map(AppUser::getUser_id);
    }
}

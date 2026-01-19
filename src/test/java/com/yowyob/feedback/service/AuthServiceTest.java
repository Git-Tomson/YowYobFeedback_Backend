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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests registration, login, and authentication-related operations.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private AppUserRepository app_user_repository;

    @Mock
    private UserMapper user_mapper;

    @Mock
    private PasswordEncoder password_encoder;

    @Mock
    private OrganizationRepository organization_repository;

    @Mock
    private PersonRepository person_repository;

    @Mock
    private JwtService jwt_service;

    @Mock
    private TwoFactorService two_factor_service;

    @InjectMocks
    private AuthService auth_service;

    private RegisterRequestDTO valid_person_request;
    private RegisterRequestDTO valid_organization_request;
    private AppUser test_user;
    private Person test_person;
    private Organization test_organization;

    @BeforeEach
    void setUp() {
        valid_person_request = RegisterRequestDTO.builder()
                .user_type(UserType.PERSON)
                .user_firstname("John")
                .user_lastname("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .contact("+237123456789")
                .occupation("Software Developer")
                .build();

        valid_organization_request = RegisterRequestDTO.builder()
                .user_type(UserType.ORGANIZATION)
                .user_lastname("TechCorp")
                .email("contact@techcorp.com")
                .password("password123")
                .contact("+237987654321")
                .location("Yaoundé, Cameroon")
                .build();

        test_user = AppUser.builder()
                .user_id(UUID.randomUUID())
                .user_type(UserType.PERSON)
                .user_firstname("John")
                .user_lastname("Doe")
                .email("john.doe@example.com")
                .password("encoded_password")
                .contact("+237123456789")
                .registration_date_time(OffsetDateTime.now())
                .certified(false)
                .two_fa_enabled(false)
                .build();

        test_person = Person.builder()
                .person_id(test_user.getUser_id())
                .occupation("Software Developer")
                .build();

        test_organization = Organization.builder()
                .org_id(UUID.randomUUID())
                .location("Yaoundé, Cameroon")
                .build();
    }

    @Test
    @DisplayName("Should register person successfully")
    void shouldRegisterPersonSuccessfully() {
        when(app_user_repository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(app_user_repository.existsByContact(anyString())).thenReturn(Mono.just(false));
        when(user_mapper.toUserEntity(any(RegisterRequestDTO.class))).thenReturn(test_user);
        when(password_encoder.encode(anyString())).thenReturn("encoded_password");
        when(app_user_repository.save(any(AppUser.class))).thenReturn(Mono.just(test_user));
        when(user_mapper.toPersonEntity(any(RegisterRequestDTO.class))).thenReturn(test_person);
        when(person_repository.save(any(Person.class))).thenReturn(Mono.just(test_person));
        when(person_repository.findByPersonId(any(UUID.class))).thenReturn(Mono.just(test_person));
        when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(
                UserResponseDTO.builder()
                        .user_type(UserType.PERSON)
                        .user_firstname("John")
                        .user_lastname("Doe")
                        .email("john.doe@example.com")
                        .occupation("Software Developer")
                        .build()
        );
        when(jwt_service.generateToken(any())).thenReturn("fake-jwt-token");

        StepVerifier.create(auth_service.register(valid_person_request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.message()).isEqualTo(AppConstants.REGISTRATION_SUCCESS_MESSAGE);
                    assertThat(response.user_response_dto()).isNotNull();
                    assertThat(response.user_response_dto().email()).isEqualTo("john.doe@example.com");
                    assertThat(response.user_response_dto().occupation()).isEqualTo("Software Developer");
                    assertThat(response.token()).isNotNull();
                })
                .verifyComplete();

        verify(app_user_repository, times(1)).save(any(AppUser.class));
        verify(person_repository, times(1)).save(any(Person.class));
    }

    @Test
    @DisplayName("Should register organization successfully")
    void shouldRegisterOrganizationSuccessfully() {
        AppUser org_user = AppUser.builder()
                .user_id(UUID.randomUUID())
                .user_type(UserType.ORGANIZATION)
                .user_lastname("TechCorp")
                .email("contact@techcorp.com")
                .password("encoded_password")
                .contact("+237987654321")
                .registration_date_time(OffsetDateTime.now())
                .certified(false)
                .build();

        when(app_user_repository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(app_user_repository.existsByContact(anyString())).thenReturn(Mono.just(false));
        when(user_mapper.toUserEntity(any(RegisterRequestDTO.class))).thenReturn(org_user);
        when(password_encoder.encode(anyString())).thenReturn("encoded_password");
        when(app_user_repository.save(any(AppUser.class))).thenReturn(Mono.just(org_user));
        when(user_mapper.toOrganizationEntity(any(RegisterRequestDTO.class))).thenReturn(test_organization);
        when(organization_repository.save(any(Organization.class))).thenReturn(Mono.just(test_organization));
        when(organization_repository.findByOrgId(any(UUID.class))).thenReturn(Mono.just(test_organization));
        when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(
                UserResponseDTO.builder()
                        .user_type(UserType.ORGANIZATION)
                        .user_lastname("TechCorp")
                        .email("contact@techcorp.com")
                        .location("Yaoundé, Cameroon")
                        .build()
        );

        StepVerifier.create(auth_service.register(valid_organization_request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.message()).isEqualTo(AppConstants.REGISTRATION_SUCCESS_MESSAGE);
                    assertThat(response.user_response_dto()).isNotNull();
                    assertThat(response.user_response_dto().user_type()).isEqualTo(UserType.ORGANIZATION);
                    assertThat(response.user_response_dto().location()).isEqualTo("Yaoundé, Cameroon");
                })
                .verifyComplete();

        verify(app_user_repository, times(1)).save(any(AppUser.class));
        verify(organization_repository, times(1)).save(any(Organization.class));
    }

    @Test
    @DisplayName("Should fail registration when email already exists")
    void shouldFailRegistrationWhenEmailExists() {
        when(app_user_repository.existsByEmail(anyString())).thenReturn(Mono.just(true));
        when(app_user_repository.existsByContact(anyString())).thenReturn(Mono.just(false));

        StepVerifier.create(auth_service.register(valid_person_request))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.USER_ALREADY_EXISTS_MESSAGE)
                )
                .verify();

        verify(app_user_repository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should fail registration when contact already exists")
    void shouldFailRegistrationWhenContactExists() {
        when(app_user_repository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(app_user_repository.existsByContact(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(auth_service.register(valid_person_request))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.USER_ALREADY_EXISTS_MESSAGE)
                )
                .verify();

        verify(app_user_repository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should fail registration when neither email nor contact provided")
    void shouldFailRegistrationWhenNoContactInfo() {
        RegisterRequestDTO invalid_request = RegisterRequestDTO.builder()
                .user_type(UserType.PERSON)
                .user_lastname("Doe")
                .password("password123")
                .occupation("Developer")
                .build();

        StepVerifier.create(auth_service.register(invalid_request))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.EMAIL_OR_CONTACT_REQUIRED_MESSAGE)
                )
                .verify();

        verify(app_user_repository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should fail registration when occupation missing for PERSON")
    void shouldFailRegistrationWhenOccupationMissing() {
        RegisterRequestDTO invalid_request = RegisterRequestDTO.builder()
                .user_type(UserType.PERSON)
                .user_lastname("Doe")
                .email("john@example.com")
                .password("password123")
                .build();

        StepVerifier.create(auth_service.register(invalid_request))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.OCCUPATION_REQUIRED_FOR_PERSON_MESSAGE)
                )
                .verify();

        verify(app_user_repository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should fail registration when location missing for ORGANIZATION")
    void shouldFailRegistrationWhenLocationMissing() {
        RegisterRequestDTO invalid_request = RegisterRequestDTO.builder()
                .user_type(UserType.ORGANIZATION)
                .user_lastname("TechCorp")
                .email("contact@techcorp.com")
                .password("password123")
                .build();

        StepVerifier.create(auth_service.register(invalid_request))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.LOCATION_REQUIRED_FOR_ORGANIZATION_MESSAGE)
                )
                .verify();

        verify(app_user_repository, never()).save(any(AppUser.class));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        LoginRequestDTO login_request = LoginRequestDTO.builder()
                .identifier("john.doe@example.com")
                .password("password123")
                .build();

        when(app_user_repository.findByEmailOrContact(anyString())).thenReturn(Mono.just(test_user));
        when(password_encoder.matches(anyString(), anyString())).thenReturn(true);
        when(person_repository.findByPersonId(any(UUID.class))).thenReturn(Mono.just(test_person));
        when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(
                UserResponseDTO.builder()
                        .user_type(UserType.PERSON)
                        .email("john.doe@example.com")
                        .occupation("Software Developer")
                        .build()
        );
        when(jwt_service.generateToken(any(AppUser.class))).thenReturn("jwt_token");

        StepVerifier.create(auth_service.login(login_request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.message()).isEqualTo(AppConstants.LOGIN_SUCCESS_MESSAGE);
                    assertThat(response.token()).isEqualTo("jwt_token");
                    assertThat(response.user_response_dto()).isNotNull();
                })
                .verifyComplete();

        verify(app_user_repository, times(1)).findByEmailOrContact(anyString());
        verify(password_encoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should fail login with invalid password")
    void shouldFailLoginWithInvalidPassword() {
        LoginRequestDTO login_request = LoginRequestDTO.builder()
                .identifier("john.doe@example.com")
                .password("wrong_password")
                .build();

        when(app_user_repository.findByEmailOrContact(anyString())).thenReturn(Mono.just(test_user));
        when(password_encoder.matches(anyString(), anyString())).thenReturn(false);

        StepVerifier.create(auth_service.login(login_request))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.INVALID_PASSWORD_MESSAGE)
                )
                .verify();

        verify(jwt_service, never()).generateToken(any(AppUser.class));
    }

    @Test
    @DisplayName("Should fail login when user not found")
    void shouldFailLoginWhenUserNotFound() {
        LoginRequestDTO login_request = LoginRequestDTO.builder()
                .identifier("nonexistent@example.com")
                .password("password123")
                .build();

        when(app_user_repository.findByEmailOrContact(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(auth_service.login(login_request))
                .expectErrorMatches(error ->
                        error instanceof IllegalArgumentException &&
                                error.getMessage().equals(AppConstants.USER_NOT_FOUND_MESSAGE)
                )
                .verify();

        verify(password_encoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should get current user successfully")
    void shouldGetCurrentUserSuccessfully() {
        when(app_user_repository.findByEmailOrContact(anyString())).thenReturn(Mono.just(test_user));
        when(person_repository.findByPersonId(any(UUID.class))).thenReturn(Mono.just(test_person));
        when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(
                UserResponseDTO.builder()
                        .user_type(UserType.PERSON)
                        .email("john.doe@example.com")
                        .occupation("Software Developer")
                        .build()
        );

        StepVerifier.create(auth_service.getCurrentUser("john.doe@example.com"))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.email()).isEqualTo("john.doe@example.com");
                    assertThat(response.occupation()).isEqualTo("Software Developer");
                })
                .verifyComplete();

        verify(app_user_repository, times(1)).findByEmailOrContact(anyString());
    }

    @Test
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() {
        StepVerifier.create(auth_service.logout())
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response).containsKey("message");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should get user ID by identifier successfully")
    void shouldGetUserIdByIdentifier() {
        when(app_user_repository.findByEmailOrContact(anyString())).thenReturn(Mono.just(test_user));

        StepVerifier.create(auth_service.getUserIdByIdentifier("john.doe@example.com"))
                .assertNext(user_id -> {
                    assertThat(user_id).isNotNull();
                    assertThat(user_id).isEqualTo(test_user.getUser_id());
                })
                .verifyComplete();
    }
}

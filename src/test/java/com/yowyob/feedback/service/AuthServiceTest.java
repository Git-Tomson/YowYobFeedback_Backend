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
import org.junit.jupiter.api.Nested;
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
 * Tests user registration, login, and authentication logic.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private AppUserRepository app_user_repository;

    @Mock
    private PersonRepository person_repository;

    @Mock
    private OrganizationRepository organization_repository;

    @Mock
    private UserMapper user_mapper;

    @Mock
    private PasswordEncoder password_encoder;

    @Mock
    private JwtService jwt_service;

    @Mock
    private TwoFactorService two_factor_service;

    @InjectMocks
    private AuthService auth_service;

    private RegisterRequestDTO person_register_request;
    private RegisterRequestDTO organization_register_request;
    private LoginRequestDTO login_request;
    private AppUser test_person_user;
    private AppUser test_organization_user;
    private Person test_person;
    private Organization test_organization;
    private UUID test_user_id;

    @BeforeEach
    void setUp() {
        test_user_id = UUID.randomUUID();

        // Person registration request
        person_register_request = RegisterRequestDTO.builder()
                .user_type(UserType.PERSON)
                .user_firstname("John")
                .user_lastname("Doe")
                .email("john.doe@example.com")
                .password("SecurePass123")
                .contact("+237123456789")
                .occupation("Software Engineer")
                .build();

        // Organization registration request
        organization_register_request = RegisterRequestDTO.builder()
                .user_type(UserType.ORGANIZATION)
                .user_firstname("Tech")
                .user_lastname("Corp")
                .email("contact@techcorp.com")
                .password("SecurePass123")
                .location("Yaoundé, Cameroon")
                .build();

        // Login request
        login_request = LoginRequestDTO.builder()
                .identifier("john.doe@example.com")
                .password("SecurePass123")
                .build();

        // Test person user
        test_person_user = AppUser.builder()
                .user_id(test_user_id)
                .user_type(UserType.PERSON)
                .user_firstname("John")
                .user_lastname("Doe")
                .email("john.doe@example.com")
                .password("encoded_password")
                .contact("+237123456789")
                .registration_date_time(OffsetDateTime.now())
                .certified(false)
                .build();

        // Test organization user
        test_organization_user = AppUser.builder()
                .user_id(test_user_id)
                .user_type(UserType.ORGANIZATION)
                .user_firstname("Tech")
                .user_lastname("Corp")
                .email("contact@techcorp.com")
                .password("encoded_password")
                .registration_date_time(OffsetDateTime.now())
                .certified(false)
                .build();

        // Test person entity
        test_person = Person.builder()
                .person_id(test_user_id)
                .occupation("Software Engineer")
                .build();

        // Test organization entity
        test_organization = Organization.builder()
                .org_id(test_user_id)
                .location("Yaoundé, Cameroon")
                .build();
    }

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register person successfully")
        void shouldRegisterPersonSuccessfully() {
            // Given
            when(app_user_repository.existsByEmail(anyString())).thenReturn(Mono.just(false));
            when(app_user_repository.existsByContact(anyString())).thenReturn(Mono.just(false));
            when(user_mapper.toUserEntity(any(RegisterRequestDTO.class))).thenReturn(test_person_user);
            when(password_encoder.encode(anyString())).thenReturn("encoded_password");
            when(app_user_repository.save(any(AppUser.class))).thenReturn(Mono.just(test_person_user));
            when(user_mapper.toPersonEntity(any(RegisterRequestDTO.class))).thenReturn(test_person);
            when(person_repository.save(any(Person.class))).thenReturn(Mono.just(test_person));
            when(person_repository.findByPersonId(any(UUID.class))).thenReturn(Mono.just(test_person));
            when(jwt_service.generateToken(any(AppUser.class))).thenReturn("jwt_token_here");

            UserResponseDTO user_response = UserResponseDTO.builder()
                    .user_type(UserType.PERSON)
                    .user_firstname("John")
                    .user_lastname("Doe")
                    .email("john.doe@example.com")
                    .occupation("Software Engineer")
                    .build();
            when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(user_response);

            // When
            Mono<AuthResponseDTO> result = auth_service.register(person_register_request);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.message()).isEqualTo(AppConstants.REGISTRATION_SUCCESS_MESSAGE);
                        assertThat(response.token()).isEqualTo("jwt_token_here");
                        assertThat(response.user_response_dto()).isNotNull();
                        assertThat(response.user_response_dto().user_type()).isEqualTo(UserType.PERSON);
                        assertThat(response.user_response_dto().occupation()).isEqualTo("Software Engineer");
                    })
                    .verifyComplete();

            verify(app_user_repository).save(any(AppUser.class));
            verify(person_repository).save(any(Person.class));
            verify(password_encoder).encode("SecurePass123");
        }

        @Test
        @DisplayName("Should register organization successfully")
        void shouldRegisterOrganizationSuccessfully() {
            // Given
            when(app_user_repository.existsByEmail(anyString())).thenReturn(Mono.just(false));
            lenient().when(app_user_repository.existsByContact(anyString())).thenReturn(Mono.just(true)); // No contact provided
            when(user_mapper.toUserEntity(any(RegisterRequestDTO.class))).thenReturn(test_organization_user);
            when(password_encoder.encode(anyString())).thenReturn("encoded_password");
            when(app_user_repository.save(any(AppUser.class))).thenReturn(Mono.just(test_organization_user));
            when(user_mapper.toOrganizationEntity(any(RegisterRequestDTO.class))).thenReturn(test_organization);
            when(organization_repository.save(any(Organization.class))).thenReturn(Mono.just(test_organization));
            when(organization_repository.findByOrgId(any(UUID.class))).thenReturn(Mono.just(test_organization));
            when(jwt_service.generateToken(any(AppUser.class))).thenReturn("jwt_token_here");

            UserResponseDTO user_response = UserResponseDTO.builder()
                    .user_type(UserType.ORGANIZATION)
                    .user_firstname("Tech")
                    .user_lastname("Corp")
                    .email("contact@techcorp.com")
                    .location("Yaoundé, Cameroon")
                    .build();
            when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(user_response);

            // When
            Mono<AuthResponseDTO> result = auth_service.register(organization_register_request);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.user_response_dto().user_type()).isEqualTo(UserType.ORGANIZATION);
                        assertThat(response.user_response_dto().location()).isEqualTo("Yaoundé, Cameroon");
                    })
                    .verifyComplete();

            verify(organization_repository).save(any(Organization.class));
        }

        @Test
        @DisplayName("Should fail registration when email already exists")
        void shouldFailRegistrationWhenEmailAlreadyExists() {
            // Given
            when(app_user_repository.existsByEmail(anyString())).thenReturn(Mono.just(true));
            when(app_user_repository.existsByContact(anyString())).thenReturn(Mono.just(false));

            // When
            Mono<AuthResponseDTO> result = auth_service.register(person_register_request);

            // Then
            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof IllegalArgumentException &&
                                    error.getMessage().equals(AppConstants.USER_ALREADY_EXISTS_MESSAGE))
                    .verify();

            verify(app_user_repository, never()).save(any(AppUser.class));
        }

        @Test
        @DisplayName("Should fail registration when contact already exists")
        void shouldFailRegistrationWhenContactAlreadyExists() {
            // Given
            when(app_user_repository.existsByEmail(anyString())).thenReturn(Mono.just(false));
            when(app_user_repository.existsByContact(anyString())).thenReturn(Mono.just(true));

            // When
            Mono<AuthResponseDTO> result = auth_service.register(person_register_request);

            // Then
            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof IllegalArgumentException &&
                                    error.getMessage().equals(AppConstants.USER_ALREADY_EXISTS_MESSAGE))
                    .verify();
        }

        @Test
        @DisplayName("Should fail registration when email and contact are both null")
        void shouldFailRegistrationWhenEmailAndContactAreNull() {
            // Given
            RegisterRequestDTO invalid_request = RegisterRequestDTO.builder()
                    .user_type(UserType.PERSON)
                    .user_lastname("Doe")
                    .password("SecurePass123")
                    .occupation("Engineer")
                    .build();

            // When
            Mono<AuthResponseDTO> result = auth_service.register(invalid_request);

            // Then
            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof IllegalArgumentException &&
                                    error.getMessage().equals(AppConstants.EMAIL_OR_CONTACT_REQUIRED_MESSAGE))
                    .verify();
        }

        @Test
        @DisplayName("Should fail registration when occupation is missing for PERSON")
        void shouldFailRegistrationWhenOccupationIsMissing() {
            // Given
            RegisterRequestDTO invalid_request = RegisterRequestDTO.builder()
                    .user_type(UserType.PERSON)
                    .user_lastname("Doe")
                    .email("john@example.com")
                    .password("SecurePass123")
                    .build();

            // When
            Mono<AuthResponseDTO> result = auth_service.register(invalid_request);

            // Then
            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof IllegalArgumentException &&
                                    error.getMessage().equals(AppConstants.OCCUPATION_REQUIRED_FOR_PERSON_MESSAGE))
                    .verify();
        }

        @Test
        @DisplayName("Should fail registration when location is missing for ORGANIZATION")
        void shouldFailRegistrationWhenLocationIsMissing() {
            // Given
            RegisterRequestDTO invalid_request = RegisterRequestDTO.builder()
                    .user_type(UserType.ORGANIZATION)
                    .user_lastname("Corp")
                    .email("contact@corp.com")
                    .password("SecurePass123")
                    .build();

            // When
            Mono<AuthResponseDTO> result = auth_service.register(invalid_request);

            // Then
            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof IllegalArgumentException &&
                                    error.getMessage().equals(AppConstants.LOCATION_REQUIRED_FOR_ORGANIZATION_MESSAGE))
                    .verify();
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with correct credentials")
        void shouldLoginSuccessfully() {
            // Given
            when(app_user_repository.findByEmailOrContact(anyString()))
                    .thenReturn(Mono.just(test_person_user));
            when(password_encoder.matches(anyString(), anyString())).thenReturn(true);
            when(person_repository.findByPersonId(any(UUID.class))).thenReturn(Mono.just(test_person));
            when(jwt_service.generateToken(any(AppUser.class))).thenReturn("jwt_token_here");

            UserResponseDTO user_response = UserResponseDTO.builder()
                    .user_type(UserType.PERSON)
                    .email("john.doe@example.com")
                    .occupation("Software Engineer")
                    .build();
            when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(user_response);

            // When
            Mono<AuthResponseDTO> result = auth_service.login(login_request);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.message()).isEqualTo(AppConstants.LOGIN_SUCCESS_MESSAGE);
                        assertThat(response.token()).isEqualTo("jwt_token_here");
                    })
                    .verifyComplete();

            verify(password_encoder).matches("SecurePass123", "encoded_password");
        }

        @Test
        @DisplayName("Should fail login when user not found")
        void shouldFailLoginWhenUserNotFound() {
            // Given
            when(app_user_repository.findByEmailOrContact(anyString()))
                    .thenReturn(Mono.empty());

            // When
            Mono<AuthResponseDTO> result = auth_service.login(login_request);

            // Then
            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof IllegalArgumentException &&
                                    error.getMessage().equals(AppConstants.USER_NOT_FOUND_MESSAGE))
                    .verify();
        }

        @Test
        @DisplayName("Should fail login when password is incorrect")
        void shouldFailLoginWhenPasswordIsIncorrect() {
            // Given
            when(app_user_repository.findByEmailOrContact(anyString()))
                    .thenReturn(Mono.just(test_person_user));
            when(password_encoder.matches(anyString(), anyString())).thenReturn(false);

            // When
            Mono<AuthResponseDTO> result = auth_service.login(login_request);

            // Then
            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof IllegalArgumentException &&
                                    error.getMessage().equals(AppConstants.INVALID_PASSWORD_MESSAGE))
                    .verify();

            verify(jwt_service, never()).generateToken(any(AppUser.class));
        }

        @Test
        @DisplayName("Should login with contact instead of email")
        void shouldLoginWithContact() {
            // Given
            LoginRequestDTO contact_login = LoginRequestDTO.builder()
                    .identifier("+237123456789")
                    .password("SecurePass123")
                    .build();

            when(app_user_repository.findByEmailOrContact(anyString()))
                    .thenReturn(Mono.just(test_person_user));
            when(password_encoder.matches(anyString(), anyString())).thenReturn(true);
            when(person_repository.findByPersonId(any(UUID.class))).thenReturn(Mono.just(test_person));
            when(jwt_service.generateToken(any(AppUser.class))).thenReturn("jwt_token_here");

            UserResponseDTO user_response = UserResponseDTO.builder()
                    .user_type(UserType.PERSON)
                    .build();
            when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(user_response);

            // When
            Mono<AuthResponseDTO> result = auth_service.login(contact_login);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.message()).isEqualTo(AppConstants.LOGIN_SUCCESS_MESSAGE);
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("Get Current User Tests")
    class GetCurrentUserTests {

        @Test
        @DisplayName("Should get current user by email")
        void shouldGetCurrentUserByEmail() {
            // Given
            when(app_user_repository.findByEmailOrContact(anyString()))
                    .thenReturn(Mono.just(test_person_user));
            when(person_repository.findByPersonId(any(UUID.class))).thenReturn(Mono.just(test_person));

            UserResponseDTO user_response = UserResponseDTO.builder()
                    .user_type(UserType.PERSON)
                    .email("john.doe@example.com")
                    .occupation("Software Engineer")
                    .build();
            when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(user_response);

            // When
            Mono<UserResponseDTO> result = auth_service.getCurrentUser("john.doe@example.com");

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.email()).isEqualTo("john.doe@example.com");
                        assertThat(response.occupation()).isEqualTo("Software Engineer");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should get current user by UUID")
        void shouldGetCurrentUserByUUID() {
            // Given
            String user_id_string = test_user_id.toString();
            when(app_user_repository.findByEmailOrContact(anyString())).thenReturn(Mono.empty());
            when(app_user_repository.findById(any(UUID.class))).thenReturn(Mono.just(test_person_user));
            when(person_repository.findByPersonId(any(UUID.class))).thenReturn(Mono.just(test_person));
            UserResponseDTO user_response = UserResponseDTO.builder()
                    .user_type(UserType.PERSON)
                    .build();
            when(user_mapper.toUserResponseDTO(any(AppUser.class))).thenReturn(user_response);

            // When
            Mono<UserResponseDTO> result = auth_service.getCurrentUser(user_id_string);

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response).isNotNull();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("Should fail when user not found")
        void shouldFailWhenUserNotFound() {
            // Given
            when(app_user_repository.findByEmailOrContact(anyString())).thenReturn(Mono.empty());

            // When
            Mono<UserResponseDTO> result = auth_service.getCurrentUser("nonexistent@example.com");

            // Then
            StepVerifier.create(result)
                    .expectErrorMatches(error ->
                            error instanceof IllegalArgumentException &&
                                    error.getMessage().equals(AppConstants.USER_NOT_FOUND_MESSAGE))
                    .verify();
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully")
        void shouldLogoutSuccessfully() {
            // When
            Mono<java.util.Map<String, String>> result = auth_service.logout();

            // Then
            StepVerifier.create(result)
                    .assertNext(response -> {
                        assertThat(response).containsKey("message");
                        assertThat(response.get("message")).contains("Logout successful");
                    })
                    .verifyComplete();
        }
    }
}

package com.yowyob.feedback.service;

import com.yowyob.feedback.constant.AppConstants;
import com.yowyob.feedback.dto.request.CreateProjectRequestDTO;
import com.yowyob.feedback.dto.request.UpdateProjectRequestDTO;
import com.yowyob.feedback.dto.response.MemberResponseDTO;
import com.yowyob.feedback.dto.response.ProjectDetailResponseDTO;
import com.yowyob.feedback.dto.response.ProjectResponseDTO;
import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.Member;
import com.yowyob.feedback.entity.Project;
import com.yowyob.feedback.mapper.MemberMapper;
import com.yowyob.feedback.mapper.ProjectMapper;
import com.yowyob.feedback.repository.AppUserRepository;
import com.yowyob.feedback.repository.MemberRepository;
import com.yowyob.feedback.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the ProjectService class.
 *
 * @author Thomas Djotio Ndié
 * @since 2026-01-20
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private ProjectService projectService;

    private UUID creatorId;
    private UUID userId;
    private UUID projectId;
    private UUID memberId;
    private String projectName;
    private String projectCode;
    private OffsetDateTime createdAt;
    private Project project;
    private Member member;
    private AppUser appUser;
    private CreateProjectRequestDTO createRequest;
    private UpdateProjectRequestDTO updateRequest;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        userId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        projectName = "Test Project";
        projectCode = "ABC123";
        createdAt = OffsetDateTime.parse("2023-01-01T00:00:00Z");

        project = Project.builder()
                .project_id(projectId)
                .project_name(projectName)
                .code(projectCode)
                .creator_id(creatorId)
                .number_of_members(1)
                .build();

        member = Member.builder()
                .member_id(memberId)
                .member_pseudo("Test Pseudo")
                .user_id(userId)
                .project_id(projectId)
                .build();

        appUser = AppUser.builder()
                .user_id(userId)
                .user_firstname("First")
                .user_lastname("Last")
                .build();

        createRequest = new CreateProjectRequestDTO(projectName, "Description", "logo.png");
        updateRequest = new UpdateProjectRequestDTO("New Name", "New Code", "New Description", "new_logo.png");
    }

    /**
     * Verifies that a project is created successfully.
     */
    @Test
    void shouldCreateProjectSuccessfully() {
        when(projectRepository.existsByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(false));
        when(projectMapper.toEntity(any(CreateProjectRequestDTO.class), eq(creatorId), any(String.class))).thenReturn(project);
        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(project));
        when(appUserRepository.findById(creatorId)).thenReturn(Mono.just(appUser));
        when(memberRepository.save(any(Member.class))).thenReturn(Mono.just(member));
        when(projectRepository.findById(projectId)).thenReturn(Mono.just(project));
        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(project));
        when(projectMapper.toResponseDTO(project)).thenReturn(new ProjectResponseDTO(projectId, projectName, projectCode, "Description", "logo.png", createdAt, 1, creatorId));

        Mono<ProjectResponseDTO> result = projectService.createProject(createRequest, creatorId);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.project_id().equals(projectId))
                .verifyComplete();
    }

    /**
     * Verifies that project creation fails when the name already exists.
     */
    @Test
    void shouldFailToCreateProjectWhenNameAlreadyExists() {
        when(projectRepository.existsByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(true));

        Mono<ProjectResponseDTO> result = projectService.createProject(createRequest, creatorId);

        StepVerifier.create(result)
                .expectErrorMessage(AppConstants.PROJECT_NAME_ALREADY_EXISTS_MESSAGE)
                .verify();
    }

    /**
     * Verifies that projects created by a user are retrieved successfully.
     */
    @Test
    void shouldGetProjectsCreatedByUserSuccessfully() {
        when(projectRepository.findByCreatorId(creatorId)).thenReturn(Flux.just(project));
        when(projectMapper.toResponseDTO(project)).thenReturn(new ProjectResponseDTO(projectId, projectName, projectCode, "Description", "logo.png", createdAt, 1, creatorId));

        Flux<ProjectResponseDTO> result = projectService.getProjectsCreatedByUser(creatorId);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    /**
     * Verifies that projects joined by a user are retrieved successfully.
     */
    @Test
    void shouldGetProjectsJoinedByUserSuccessfully() {
        when(projectRepository.findProjectsByMemberUserId(userId)).thenReturn(Flux.just(project));
        when(projectMapper.toResponseDTO(project)).thenReturn(new ProjectResponseDTO(projectId, projectName, projectCode, "Description", "logo.png", createdAt, 1, creatorId));

        Flux<ProjectResponseDTO> result = projectService.getProjectsJoinedByUser(userId);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    /**
     * Verifies that all projects for a user are retrieved successfully.
     */
    @Test
    void shouldGetAllUserProjectsSuccessfully() {
        when(projectRepository.findByCreatorId(userId)).thenReturn(Flux.just(project));
        when(projectRepository.findProjectsByMemberUserId(userId)).thenReturn(Flux.empty());
        when(projectMapper.toResponseDTO(project)).thenReturn(new ProjectResponseDTO(projectId, projectName, projectCode, "Description", "logo.png", createdAt, 1, creatorId));

        Flux<ProjectResponseDTO> result = projectService.getAllUserProjects(userId);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    /**
     * Verifies that a member is retrieved by ID successfully.
     */
    @Test
    void shouldGetMemberByIdSuccessfully() {
        when(projectRepository.findById(projectId)).thenReturn(Mono.just(project));
        when(memberRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(Mono.just(true));
        when(memberRepository.findById(memberId)).thenReturn(Mono.just(member));
        when(appUserRepository.findById(userId)).thenReturn(Mono.just(appUser));
        when(memberMapper.toResponseDTO(member, appUser)).thenReturn(new MemberResponseDTO(memberId, "Test Pseudo", userId, projectId, "First", "Last", "email@example.com"));
        // Stub the one-arg overload to avoid strict stubbing issues due to eager evaluation
        when(memberMapper.toResponseDTO(member)).thenReturn(new MemberResponseDTO(memberId, "Test Pseudo", userId, projectId, null, null, null));

        Mono<MemberResponseDTO> result = projectService.getMemberById(projectId, memberId, userId);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.member_id().equals(memberId))
                .verifyComplete();
    }

    /**
     * Verifies that getting a member fails when the project is not found.
     */
    @Test
    void shouldFailToGetMemberByIdWhenProjectNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Mono.empty());
        when(memberRepository.findById(memberId)).thenReturn(Mono.empty());

        Mono<MemberResponseDTO> result = projectService.getMemberById(projectId, memberId, userId);

        StepVerifier.create(result)
                .expectErrorMessage(AppConstants.PROJECT_NOT_FOUND_MESSAGE)
                .verify();
    }

    /**
     * Verifies that a member's pseudo is updated successfully.
     */
    @Test
    void shouldUpdateMemberPseudoSuccessfully() {
        String newPseudo = "New Pseudo";
        Member updatedMember = Member.builder()
                .member_id(memberId)
                .member_pseudo(newPseudo)
                .user_id(userId)
                .project_id(projectId)
                .build();
        when(memberRepository.findById(memberId)).thenReturn(Mono.just(member));
        when(memberRepository.existsByMemberPseudoAndProjectId(newPseudo, projectId)).thenReturn(Mono.just(false));
        when(memberRepository.save(any(Member.class))).thenReturn(Mono.just(updatedMember));
        when(appUserRepository.findById(userId)).thenReturn(Mono.just(appUser));
        when(memberMapper.toResponseDTO(updatedMember, appUser)).thenReturn(new MemberResponseDTO(memberId, newPseudo, userId, projectId, "First", "Last", "email@example.com"));
        // Stub the one-arg overload to avoid strict stubbing issues due to eager evaluation
        when(memberMapper.toResponseDTO(updatedMember)).thenReturn(new MemberResponseDTO(memberId, newPseudo, userId, projectId, null, null, null));

        Mono<MemberResponseDTO> result = projectService.updateMemberPseudo(projectId, memberId, newPseudo, userId);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.member_pseudo().equals(newPseudo))
                .verifyComplete();
    }

    /**
     * Verifies that updating a member's pseudo fails when unauthorized.
     */
    @Test
    void shouldFailToUpdateMemberPseudoWhenUnauthorized() {
        when(memberRepository.findById(memberId)).thenReturn(Mono.just(member));

        Mono<MemberResponseDTO> result = projectService.updateMemberPseudo(projectId, memberId, "New Pseudo", UUID.randomUUID());

        StepVerifier.create(result)
                .expectErrorMessage(AppConstants.PROJECT_UNAUTHORIZED_MESSAGE)
                .verify();
    }

    /**
     * Verifies that a member is removed by name successfully.
     */
    @Test
    void shouldRemoveMemberByNameSuccessfully() {
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));
        when(memberRepository.findById(memberId)).thenReturn(Mono.just(member));
        when(projectRepository.findById(projectId)).thenReturn(Mono.just(project));
        when(memberRepository.delete(member)).thenReturn(Mono.empty());
        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(project));

        Mono<Void> result = projectService.removeMemberByName(projectName, memberId, creatorId);

        StepVerifier.create(result)
                .verifyComplete();
    }

    /**
     * Verifies that removing the creator by name fails.
     */
    @Test
    void shouldFailToRemoveCreatorByName() {
        member.setUser_id(creatorId);
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));
        when(memberRepository.findById(memberId)).thenReturn(Mono.just(member));
        when(projectRepository.findById(projectId)).thenReturn(Mono.just(project));

        Mono<Void> result = projectService.removeMemberByName(projectName, memberId, creatorId);

        StepVerifier.create(result)
                .expectErrorMessage(AppConstants.CANNOT_REMOVE_CREATOR_MESSAGE)
                .verify();
    }

    /**
     * Verifies that project details are retrieved by name successfully.
     */
    @Test
    void shouldGetProjectDetailsByNameSuccessfully() {
        when(projectRepository.findByProjectNameAndCreatorId(projectName, userId)).thenReturn(Mono.just(project));
        when(memberRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(Mono.just(true));
        when(memberRepository.findByProjectId(projectId)).thenReturn(Flux.just(member));
        when(appUserRepository.findById(userId)).thenReturn(Mono.just(appUser));
        when(memberMapper.toResponseDTO(member, appUser)).thenReturn(new MemberResponseDTO(memberId, "Pseudo", userId, projectId, "First", "Last", "email"));
        // Stub the one-arg overload to avoid strict stubbing issues due to eager evaluation
        when(memberMapper.toResponseDTO(member)).thenReturn(new MemberResponseDTO(memberId, "Pseudo", userId, projectId, null, null, null));
        when(projectMapper.toDetailResponseDTOBuilder(project)).thenReturn(ProjectDetailResponseDTO.builder()
                .project_id(projectId)
                .project_name(projectName)
                .code(projectCode)
                .creator_id(creatorId)
                .number_of_members(1)
                .description("Desc")
                .project_logo("logo.png"));

        Mono<ProjectDetailResponseDTO> result = projectService.getProjectDetailsByName(projectName, userId);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.project_id().equals(projectId))
                .verifyComplete();
    }

    /**
     * Verifies that a project is updated by name successfully.
     */
    @Test
    void shouldUpdateProjectByNameSuccessfully() {
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));
        when(projectRepository.existsByProjectNameAndCreatorId("New Name", creatorId)).thenReturn(Mono.just(false));
        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(project));
        when(projectMapper.toResponseDTO(project)).thenReturn(new ProjectResponseDTO(projectId, "New Name", "New Code", "New Description", "new_logo.png", createdAt, 1, creatorId));

        Mono<ProjectResponseDTO> result = projectService.updateProjectByName(projectName, updateRequest, creatorId);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.project_name().equals("New Name"))
                .verifyComplete();
    }

    /**
     * Verifies that the project code is regenerated by name successfully.
     */
    @Test
    void shouldRegenerateProjectCodeByNameSuccessfully() {
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));
        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(project));
        when(projectMapper.toResponseDTO(project)).thenReturn(new ProjectResponseDTO(projectId, projectName, "NEW123", "Description", "logo.png", createdAt, 1, creatorId));

        Mono<ProjectResponseDTO> result = projectService.regenerateProjectCodeByName(projectName, creatorId);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.project_id().equals(projectId))
                .verifyComplete();
    }

    /**
     * Verifies that a project is deleted by name successfully.
     */
    @Test
    void shouldDeleteProjectByNameSuccessfully() {
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));
        when(projectRepository.delete(project)).thenReturn(Mono.empty());

        Mono<Void> result = projectService.deleteProjectByName(projectName, creatorId);

        StepVerifier.create(result)
                .verifyComplete();
    }

    /**
     * Verifies that joining a project by name is successful.
     */
    @Test
    void shouldJoinProjectByNameSuccessfully() {
        String pseudo = "Join Pseudo";
        Member joinedMember = Member.builder()
                .member_id(memberId)
                .member_pseudo(pseudo)
                .user_id(userId)
                .project_id(projectId)
                .build();
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));
        when(memberRepository.existsByUserIdAndProjectId(userId, projectId)).thenReturn(Mono.just(false));
        when(memberRepository.existsByMemberPseudoAndProjectId(pseudo, projectId)).thenReturn(Mono.just(false));
        when(memberRepository.save(any(Member.class))).thenReturn(Mono.just(joinedMember));
        when(projectRepository.findById(projectId)).thenReturn(Mono.just(project));
        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(project));
        when(appUserRepository.findById(userId)).thenReturn(Mono.just(appUser));
        when(memberMapper.toResponseDTO(joinedMember, appUser)).thenReturn(new MemberResponseDTO(memberId, pseudo, userId, projectId, "First", "Last", "email"));
        // Stub the one-arg overload to avoid strict stubbing issues due to eager evaluation
        when(memberMapper.toResponseDTO(joinedMember)).thenReturn(new MemberResponseDTO(memberId, pseudo, userId, projectId, null, null, null));

        Mono<MemberResponseDTO> result = projectService.joinProjectByName(projectName, creatorId, projectCode, pseudo, userId);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.member_pseudo().equals(pseudo))
                .verifyComplete();
    }

    /**
     * Verifies that joining a project fails with an invalid code.
     */
    @Test
    void shouldFailToJoinProjectByNameWithInvalidCode() {
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));

        Mono<MemberResponseDTO> result = projectService.joinProjectByName(projectName, creatorId, "WRONG", "Pseudo", userId);

        StepVerifier.create(result)
                .expectErrorMessage(AppConstants.PROJECT_CODE_INVALID_MESSAGE)
                .verify();
    }

    /**
     * Verifies that project members are retrieved by name successfully.
     */
    @Test
    void shouldGetProjectMembersByNameSuccessfully() {
        when(projectRepository.findByProjectNameAndCreatorId(projectName, userId)).thenReturn(Mono.just(project));
        when(memberRepository.findByProjectId(projectId)).thenReturn(Flux.just(member));
        when(appUserRepository.findById(userId)).thenReturn(Mono.just(appUser));
        when(memberMapper.toResponseDTO(member, appUser)).thenReturn(new MemberResponseDTO(memberId, "Pseudo", userId, projectId, "First", "Last", "email"));
        // Stub the one-arg overload to avoid strict stubbing issues due to eager evaluation
        when(memberMapper.toResponseDTO(member)).thenReturn(new MemberResponseDTO(memberId, "Pseudo", userId, projectId, null, null, null));

        Flux<MemberResponseDTO> result = projectService.getProjectMembersByName(projectName, userId);

        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    /**
     * Verifies that leaving a project by name is successful.
     */
    @Test
    void shouldLeaveProjectByNameSuccessfully() {
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));
        when(memberRepository.deleteByUserIdAndProjectId(userId, projectId)).thenReturn(Mono.empty());
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));
        when(projectRepository.save(any(Project.class))).thenReturn(Mono.just(project));

        Mono<Void> result = projectService.leaveProjectByName(projectName, creatorId, userId);

        StepVerifier.create(result)
                .verifyComplete();
    }

    /**
     * Verifies that leaving one's own project by name fails.
     */
    @Test
    void shouldFailToLeaveOwnProjectByName() {
        when(projectRepository.findByProjectNameAndCreatorId(projectName, creatorId)).thenReturn(Mono.just(project));

        Mono<Void> result = projectService.leaveProjectByName(projectName, creatorId, creatorId);

        StepVerifier.create(result)
                .expectErrorMessage(AppConstants.CANNOT_LEAVE_YOUR_OWN_PROJECT_MESSAGE)
                .verify();
    }
}

package com.yowyob.feedback.service;

import com.yowyob.feedback.constant.AppConstants;
import com.yowyob.feedback.dto.request.CreateProjectRequestDTO;
import com.yowyob.feedback.dto.request.JoinProjectRequestDTO;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

/**
 * Service class handling project and member operations.
 * Manages project lifecycle and member management business logic.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository project_repository;
    private final MemberRepository member_repository;
    private final AppUserRepository app_user_repository;
    private final ProjectMapper project_mapper;
    private final MemberMapper member_mapper;

    private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Creates a new project for a user.
     *
     * Steps:
     * 1. Validates project name uniqueness for creator
     * 2. Generates unique 6-character code
     * 3. Creates and saves project
     * 4. Automatically adds creator as first member
     *
     * @param request the project creation data
     * @param creator_id the ID of the user creating the project
     * @return Mono<ProjectResponseDTO> the created project
     */
    @Transactional
    public Mono<ProjectResponseDTO> createProject(CreateProjectRequestDTO request,
                                                  UUID creator_id) {
        log.info("Creating project '{}' for user {}", request.project_name(), creator_id);

        return validateProjectNameUniqueness(request.project_name(), creator_id)
                .then(Mono.fromCallable(this::generateProjectCode))
                .flatMap(code -> {
                    Project project = project_mapper.toEntity(request, creator_id, code);
                    project.setNew(true);
                    return project_repository.save(project);
                })
                .flatMap(this::addCreatorAsMember)
                .map(project_mapper::toResponseDTO)
                .doOnSuccess(response -> log.info("Project created successfully with ID: {}",
                        response.project_id()))
                .doOnError(error -> log.error("Failed to create project: {}",
                        error.getMessage()));
    }

    /**
     * Retrieves all projects created by a specific user.
     *
     * @param creator_id the creator's user ID
     * @return Flux<ProjectResponseDTO> list of created projects
     */
    public Flux<ProjectResponseDTO> getProjectsCreatedByUser(UUID creator_id) {
        log.info("Fetching projects created by user {}", creator_id);

        return project_repository.findByCreatorId(creator_id)
                .map(project_mapper::toResponseDTO)
                .doOnComplete(() -> log.info("Fetched all created projects for user {}",
                        creator_id));
    }

    /**
     * Retrieves all projects where a user is a member.
     *
     * @param user_id the user ID
     * @return Flux<ProjectResponseDTO> list of joined projects
     */
    public Flux<ProjectResponseDTO> getProjectsJoinedByUser(UUID user_id) {
        log.info("Fetching projects joined by user {}", user_id);

        return project_repository.findProjectsByMemberUserId(user_id)
                .map(project_mapper::toResponseDTO)
                .doOnComplete(() -> log.info("Fetched all joined projects for user {}",
                        user_id));
    }

    /**
     * Retrieves all projects for a user (both created and joined).
     *
     * @param user_id the user ID
     * @return Flux<ProjectResponseDTO> list of all user's projects
     */
    public Flux<ProjectResponseDTO> getAllUserProjects(UUID user_id) {
        log.info("Fetching all projects for user {}", user_id);

        return Flux.merge(
                getProjectsCreatedByUser(user_id),
                getProjectsJoinedByUser(user_id)
        ).distinct(ProjectResponseDTO::project_id);
    }


    /**
     * Retrieves a specific member's information.
     *
     * @param project_id the project ID
     * @param member_id the member ID
     * @param requesting_user_id the ID of the user making the request
     * @return Mono<MemberResponseDTO> the member information
     */
    public Mono<MemberResponseDTO> getMemberById(UUID project_id, UUID member_id,
                                                 UUID requesting_user_id) {
        log.info("Fetching member {} from project {}", member_id, project_id);

        return project_repository.findById(project_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NOT_FOUND_MESSAGE)))
                .flatMap(project -> validateUserAccessToProject(project, requesting_user_id)
                        .thenReturn(project))
                .then(member_repository.findById(member_id))
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.MEMBER_NOT_FOUND_MESSAGE)))
                .flatMap(this::enrichMemberWithUserData);
    }

    /**
     * Updates a member's pseudo.
     * A member can only update their own pseudo.
     *
     * @param project_id the project ID
     * @param member_id the member ID
     * @param new_pseudo the new pseudo
     * @param requesting_user_id the ID of the user making the request
     * @return Mono<MemberResponseDTO> the updated member
     */
    @Transactional
    public Mono<MemberResponseDTO> updateMemberPseudo(UUID project_id, UUID member_id,
                                                      String new_pseudo,
                                                      UUID requesting_user_id) {
        log.info("Updating pseudo for member {} in project {}", member_id, project_id);

        return member_repository.findById(member_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.MEMBER_NOT_FOUND_MESSAGE)))
                .flatMap(member -> validateUserIsMember(member, requesting_user_id)
                        .thenReturn(member))
                .flatMap(member -> validatePseudoUniqueness(new_pseudo, project_id)
                        .thenReturn(member))
                .map(member -> {
                    member.setMember_pseudo(new_pseudo);
                    return member;
                })
                .flatMap(member_repository::save)
                .flatMap(this::enrichMemberWithUserData)
                .doOnSuccess(response -> log.info("Pseudo updated for member {}", member_id));
    }

    /**
     * Removes a member from a project by project name.
     * Only the project creator can remove members.
     * The creator cannot be removed.
     *
     * @param project_name the project name
     * @param member_id the member ID to remove
     * @param requesting_user_id the ID of the user making the request
     * @return Mono<Void>
     */
    @Transactional
    public Mono<Void> removeMemberByName(String project_name, UUID member_id,
                                         UUID requesting_user_id) {
        log.info("Removing member {} from project '{}' by user {}", member_id, project_name,
                requesting_user_id);

        return project_repository.findByProjectNameAndCreatorId(project_name, requesting_user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NOT_FOUND_MESSAGE)))
                .flatMap(project -> member_repository.findById(member_id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException(
                                AppConstants.MEMBER_NOT_FOUND_MESSAGE)))
                        .flatMap(member -> {
                            // Verify member belongs to this project
                            if (!member.getProject_id().equals(project.getProject_id())) {
                                return Mono.error(new IllegalArgumentException(
                                        AppConstants.MEMBER_NOT_FOUND_MESSAGE));
                            }
                            return validateMemberIsNotCreator(member, project.getProject_id())
                                    .thenReturn(member);
                        })
                        .flatMap(member -> member_repository.delete(member)
                                .then(decrementMemberCountByName(project_name, requesting_user_id)))
                )
                .doOnSuccess(v -> log.info("Member {} removed from project '{}'", member_id,
                        project_name));
    }

    // News méthods


    /**
     * Retrieves detailed information about a specific project by name.
     *
     * @param project_name the project name
     * @param requesting_user_id the ID of the user making the request
     * @return Mono<ProjectDetailResponseDTO> detailed project information
     */
    public Mono<ProjectDetailResponseDTO> getProjectDetailsByName(String project_name,
                                                                  UUID requesting_user_id) {
        log.info("Fetching details for project '{}'", project_name);

        return project_repository.findByProjectNameAndCreatorId(project_name, requesting_user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NOT_FOUND_MESSAGE)))
                .flatMap(project -> validateUserAccessToProject(project, requesting_user_id)
                        .thenReturn(project))
                .flatMap(project -> getProjectMembersByName(project.getProject_name(), requesting_user_id)
                        .collectList()
                        .map(members -> project_mapper.toDetailResponseDTOBuilder(project)
                                .members(members)
                                .build()))
                .doOnSuccess(response -> log.info("Fetched details for project '{}'",
                        project_name));
    }

    /**
     * Updates project information by name.
     * Only the project creator can update the project.
     *
     * @param project_name the project name
     * @param request the update data
     * @param requesting_user_id the ID of the user making the request
     * @return Mono<ProjectResponseDTO> the updated project
     */
    @Transactional
    public Mono<ProjectResponseDTO> updateProjectByName(String project_name,
                                                        UpdateProjectRequestDTO request,
                                                        UUID requesting_user_id) {
        log.info("Updating project '{}' by user {}", project_name, requesting_user_id);

        return project_repository.findByProjectNameAndCreatorId(project_name, requesting_user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NOT_FOUND_MESSAGE)))
                .flatMap(project -> validateAndApplyUpdates(project, request))
                .flatMap(project_repository::save)
                .map(project_mapper::toResponseDTO)
                .doOnSuccess(response -> log.info("Project '{}' updated successfully",
                        project_name))
                .doOnError(error -> log.error("Failed to update project '{}': {}",
                        project_name, error.getMessage()));
    }

    /**
     * Regenerates the project access code by project name.
     * Only the project creator can regenerate the code.
     *
     * @param project_name the project name
     * @param requesting_user_id the ID of the user making the request
     * @return Mono<ProjectResponseDTO> the project with new code
     */
    @Transactional
    public Mono<ProjectResponseDTO> regenerateProjectCodeByName(String project_name,
                                                                UUID requesting_user_id) {
        log.info("Regenerating code for project '{}' by user {}", project_name,
                requesting_user_id);

        return project_repository.findByProjectNameAndCreatorId(project_name, requesting_user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NOT_FOUND_MESSAGE)))
                .map(project -> {
                    project.setCode(generateProjectCode());
                    return project;
                })
                .flatMap(project_repository::save)
                .map(project_mapper::toResponseDTO)
                .doOnSuccess(response -> log.info("Code regenerated for project '{}'",
                        project_name));
    }

    /**
     * Deletes a project by name.
     * Only the project creator can delete the project.
     *
     * @param project_name the project name
     * @param requesting_user_id the ID of the user making the request
     * @return Mono<Void>
     */
    @Transactional
    public Mono<Void> deleteProjectByName(String project_name, UUID requesting_user_id) {
        log.info("Deleting project '{}' by user {}", project_name, requesting_user_id);

        return project_repository.findByProjectNameAndCreatorId(project_name, requesting_user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NOT_FOUND_MESSAGE)))
                .flatMap(project -> project_repository.delete(project))
                .doOnSuccess(v -> log.info("Project '{}' deleted successfully", project_name))
                .doOnError(error -> log.error("Failed to delete project '{}': {}",
                        project_name, error.getMessage()));
    }

    /**
     * Allows a user to join a project using project name and code.
     *
     * @param project_name the project name
     * @param creator_id the project creator's ID
     * @param code the project code
     * @param member_pseudo the desired pseudo
     * @param user_id the ID of the user joining
     * @return Mono<MemberResponseDTO> the created member
     */
    @Transactional
    public Mono<MemberResponseDTO> joinProjectByName(String project_name, UUID creator_id,
                                                     String code, String member_pseudo,
                                                     UUID user_id) {
        log.info("User {} attempting to join project '{}'", user_id, project_name);

        return validateProjectAndCodeByName(project_name, creator_id, code)
                .flatMap(project -> validateUserNotAlreadyMember(user_id, project.getProject_id())
                        .thenReturn(project))
                .flatMap(project -> validatePseudoUniqueness(member_pseudo,
                        project.getProject_id())
                        .thenReturn(project))
                .flatMap(project -> createMemberAndIncrementCountByName(member_pseudo, user_id,
                        project))
                .flatMap(member -> enrichMemberWithUserData(member))
                .doOnSuccess(response -> log.info("User {} joined project '{}' successfully",
                        user_id, project_name))
                .doOnError(error -> log.error("Failed to join project: {}",
                        error.getMessage()));
    }

    /**
     * Retrieves all members of a project by project name.
     *
     * @param project_name the project name
     * @param requesting_user_id the ID of the user making the request
     * @return Flux<MemberResponseDTO> list of members
     */
    public Flux<MemberResponseDTO> getProjectMembersByName(String project_name,
                                                           UUID requesting_user_id) {
        log.info("Fetching members for project '{}'", project_name);

        return project_repository.findByProjectNameAndCreatorId(project_name, requesting_user_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NOT_FOUND_MESSAGE)))
                .flatMapMany(project -> member_repository.findByProjectId(project.getProject_id()))
                .flatMap(this::enrichMemberWithUserData)
                .doOnComplete(() -> log.info("Fetched all members for project '{}'",
                        project_name));
    }

    /**
     * Allows a user to leave a project by project name.
     *
     * @param project_name the project name
     * @param creator_id the project creator's ID
     * @param user_id the ID of the user leaving
     * @return Mono<Void>
     */
    @Transactional
    public Mono<Void> leaveProjectByName(String project_name, UUID creator_id, UUID user_id) {
        log.info("User {} attempting to leave project '{}'", user_id, project_name);

        return project_repository.findByProjectNameAndCreatorId(project_name, creator_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NOT_FOUND_MESSAGE)))
                .flatMap(project -> {
                    // Vérification si l'utilisateur est le créateur
                    if (project.getCreator_id().equals(user_id)) {
                        return Mono.error(new IllegalArgumentException(
                                AppConstants.CANNOT_LEAVE_YOUR_OWN_PROJECT_MESSAGE));
                    }

                    // On utilise l'ID du projet déjà disponible dans l'objet 'project'
                    // On chaîne la suppression et le décrément à l'intérieur de ce flatMap
                    return member_repository.deleteByUserIdAndProjectId(user_id, project.getProject_id())
                            .then(decrementMemberCountByName(project_name, creator_id));
                })
                .doOnSuccess(v -> log.info("User {} left project '{}'", user_id, project_name));
    }


    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Generates a random 6-character alphanumeric code.
     */
    private String generateProjectCode() {
        StringBuilder code = new StringBuilder(AppConstants.PROJECT_CODE_LENGTH);
        for (int i = 0; i < AppConstants.PROJECT_CODE_LENGTH; i++) {
            code.append(ALPHANUMERIC_CHARS.charAt(
                    RANDOM.nextInt(ALPHANUMERIC_CHARS.length())));
        }
        return code.toString();
    }

    /**
     * Validates that a project name is unique for a creator.
     */
    private Mono<Void> validateProjectNameUniqueness(String project_name, UUID creator_id) {
        return project_repository.existsByProjectNameAndCreatorId(project_name, creator_id)
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NAME_ALREADY_EXISTS_MESSAGE))
                        : Mono.empty());
    }

    /**
     * Adds the project creator as the first member with their name as pseudo.
     */
    private Mono<Project> addCreatorAsMember(Project project) {
        return app_user_repository.findById(project.getCreator_id())
                .flatMap(creator -> {
                    String creator_pseudo = creator.getUser_firstname() != null
                            ? creator.getUser_firstname() + " " + creator.getUser_lastname()
                            : creator.getUser_lastname();

                    Member creator_member = Member.builder()
                            .member_pseudo(creator_pseudo)
                            .user_id(creator.getUser_id())
                            .project_id(project.getProject_id())
                            .build();
                    creator_member.setNew(true);

                    return member_repository.save(creator_member);
                })
                .then(incrementMemberCount(project.getProject_id()))
                .thenReturn(project);
    }

    /**
     * Validates user is not already a member of the project.
     */
    private Mono<Void> validateUserNotAlreadyMember(UUID user_id, UUID project_id) {
        return member_repository.existsByUserIdAndProjectId(user_id, project_id)
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalArgumentException(
                        AppConstants.MEMBER_ALREADY_EXISTS_MESSAGE))
                        : Mono.empty());
    }

    /**
     * Validates pseudo is unique within the project.
     */
    private Mono<Void> validatePseudoUniqueness(String pseudo, UUID project_id) {
        return member_repository.existsByMemberPseudoAndProjectId(pseudo, project_id)
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalArgumentException(
                        AppConstants.MEMBER_PSEUDO_ALREADY_EXISTS_MESSAGE))
                        : Mono.empty());
    }


    /**
     * Enriches member data with user information.
     */
    private Mono<MemberResponseDTO> enrichMemberWithUserData(Member member) {
        return app_user_repository.findById(member.getUser_id())
                .map(user -> member_mapper.toResponseDTO(member, user))
                .defaultIfEmpty(member_mapper.toResponseDTO(member));
    }

    /**
     * Increments the project member count.
     */
    private Mono<Void> incrementMemberCount(UUID project_id) {
        return project_repository.findById(project_id)
                .flatMap(project -> {
                    project.setNumber_of_members(project.getNumber_of_members() + 1);
                    return project_repository.save(project);
                })
                .then();
    }


    /**
     * Validates user has access to view project (is creator or member).
     */
    private Mono<Void> validateUserAccessToProject(Project project, UUID user_id) {
        if (project.getCreator_id().equals(user_id)) {
            return Mono.empty();
        }

        return member_repository.existsByUserIdAndProjectId(user_id,
                        project.getProject_id())
                .flatMap(is_member -> is_member
                        ? Mono.empty()
                        : Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_UNAUTHORIZED_MESSAGE)));
    }

    /**
     * Validates user is the project creator.
     */
    private Mono<Void> validateUserIsCreator(Project project, UUID user_id) {
        if (!project.getCreator_id().equals(user_id)) {
            return Mono.error(new IllegalArgumentException(
                    AppConstants.PROJECT_UNAUTHORIZED_MESSAGE));
        }
        return Mono.empty();
    }

    /**
     * Validates user is the member (for self-updates).
     */
    private Mono<Void> validateUserIsMember(Member member, UUID user_id) {
        if (!member.getUser_id().equals(user_id)) {
            return Mono.error(new IllegalArgumentException(
                    AppConstants.PROJECT_UNAUTHORIZED_MESSAGE));
        }
        return Mono.empty();
    }

    /**
     * Validates member is not the project creator (for removal).
     */
    private Mono<Void> validateMemberIsNotCreator(Member member, UUID project_id) {
        return project_repository.findById(project_id)
                .flatMap(project -> {
                    if (project.getCreator_id().equals(member.getUser_id())) {
                        return Mono.error(new IllegalArgumentException(
                                AppConstants.CANNOT_REMOVE_CREATOR_MESSAGE));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Validates and applies updates to project.
     */
    private Mono<Project> validateAndApplyUpdates(Project project,
                                                  UpdateProjectRequestDTO request) {
        if (request.project_name() != null && !request.project_name().isBlank()) {
            if (!request.project_name().equals(project.getProject_name())) {
                return project_repository.existsByProjectNameAndCreatorId(
                                request.project_name(), project.getCreator_id())
                        .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new IllegalArgumentException(
                                        AppConstants.PROJECT_NAME_ALREADY_EXISTS_MESSAGE));
                            }
                            project.setProject_name(request.project_name());
                            return applyRemainingUpdates(project, request);
                        });
            }
        }
        return applyRemainingUpdates(project, request);
    }

    /**
     * Applies remaining updates (code, description, logo).
     */
    private Mono<Project> applyRemainingUpdates(Project project,
                                                UpdateProjectRequestDTO request) {
        if (request.code() != null && !request.code().isBlank()) {
            project.setCode(request.code());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.project_logo() != null) {
            project.setProject_logo(request.project_logo());
        }
        return Mono.just(project);
    }

    // ==================== NOUVELLES MÉTHODES PRIVÉES ====================

    /**
     * Validates project exists and code matches by name.
     */
    private Mono<Project> validateProjectAndCodeByName(String project_name, UUID creator_id,
                                                       String code) {
        return project_repository.findByProjectNameAndCreatorId(project_name, creator_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        AppConstants.PROJECT_NOT_FOUND_MESSAGE)))
                .flatMap(project -> {
                    if (!project.getCode().equals(code)) {
                        return Mono.error(new IllegalArgumentException(
                                AppConstants.PROJECT_CODE_INVALID_MESSAGE));
                    }
                    return Mono.just(project);
                });
    }

    /**
     * Creates a member and increments project member count by name.
     */
    private Mono<Member> createMemberAndIncrementCountByName(String member_pseudo, UUID user_id,
                                                             Project project) {
        Member member = Member.builder()
                .member_pseudo(member_pseudo)
                .user_id(user_id)
                .project_id(project.getProject_id())
                .build();
        member.setNew(true);

        return member_repository.save(member)
                .flatMap(saved_member -> incrementMemberCount(project.getProject_id())
                        .thenReturn(saved_member));
    }

    /**
     * Decrements the project member count by name.
     */
    private Mono<Void> decrementMemberCountByName(String project_name, UUID creator_id) {
        return project_repository.findByProjectNameAndCreatorId(project_name, creator_id)
                .flatMap(project -> {
                    project.setNumber_of_members(Math.max(0,
                            project.getNumber_of_members() - 1));
                    return project_repository.save(project);
                })
                .then();
    }


}

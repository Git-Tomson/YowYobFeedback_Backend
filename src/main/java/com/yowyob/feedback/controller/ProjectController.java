package com.yowyob.feedback.controller;

import com.yowyob.feedback.constant.AppConstants;
import com.yowyob.feedback.dto.request.CreateProjectRequestDTO;
import com.yowyob.feedback.dto.request.UpdateProjectRequestDTO;
import com.yowyob.feedback.dto.response.MemberResponseDTO;
import com.yowyob.feedback.dto.response.ProjectDetailResponseDTO;
import com.yowyob.feedback.dto.response.ProjectResponseDTO;
import com.yowyob.feedback.service.ProjectService;
import com.yowyob.feedback.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static com.yowyob.feedback.constant.AppConstants.*;

/**
 * REST controller for project and member management endpoints.
 * Handles CRUD operations on projects and member management.
 *
 * Base path: /api/v1/projects
 *
 * Projects are identified by their name (unique per creator).
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management API")
public class ProjectController {

    private final ProjectService project_service;

    /**
     * Creates a new project.
     *
     * @param request the project creation data
     * @return Mono<ProjectResponseDTO> the created project
     */
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new project",
            description = "Creates a new project for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid data or project name already exists"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public Mono<ProjectResponseDTO> createProject(
            @Valid @RequestBody CreateProjectRequestDTO request) {
        log.info("POST /api/v1/projects - Creating project '{}'", request.project_name());
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> project_service.createProject(request, user_id));
    }

    /**
     * Retrieves detailed information about a specific project by name.
     *
     * @param project_name the project name
     * @return Mono<ProjectDetailResponseDTO> detailed project information
     */
    @GetMapping(value = "/{projectName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get project details",
            description = "Retrieves detailed information about a specific project by name"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectDetailResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized to view this project"
            )
    })
    public Mono<ProjectDetailResponseDTO> getProjectDetails(
            @PathVariable("projectName") String project_name) {
        log.info("GET /api/v1/projects/{}", project_name);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> project_service.getProjectDetailsByName(project_name, user_id));
    }

    /**
     * Updates project information.
     * Only the project creator can update the project.
     *
     * @param project_name the project name
     * @param request the update data
     * @return Mono<ProjectResponseDTO> the updated project
     */
    @PatchMapping(value = "/{projectName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update project",
            description = "Updates project information. Only the creator can update."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project updated successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized to update this project"
            )
    })
    public Mono<ProjectResponseDTO> updateProject(
            @PathVariable("projectName") String project_name,
            @Valid @RequestBody UpdateProjectRequestDTO request) {
        log.info("PATCH /api/v1/projects/{}", project_name);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> project_service.updateProjectByName(project_name, request, user_id));
    }

    /**
     * Regenerates the project access code.
     * Only the project creator can regenerate the code.
     *
     * @param project_name the project name
     * @return Mono<ProjectResponseDTO> the project with new code
     */
    @PostMapping(value = "/{projectName}/regenerate-code",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Regenerate project code",
            description = "Generates a new access code for the project"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Code regenerated successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized"
            )
    })
    public Mono<ProjectResponseDTO> regenerateProjectCode(
            @PathVariable("projectName") String project_name) {
        log.info("POST /api/v1/projects/{}/regenerate-code", project_name);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> project_service.regenerateProjectCodeByName(project_name, user_id));
    }

    /**
     * Deletes a project.
     * Only the project creator can delete the project.
     *
     * @param project_name the project name
     * @return Mono<Map> success message
     */
    @DeleteMapping(value = "/{projectName}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Delete project",
            description = "Deletes a project. Only the creator can delete."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized"
            )
    })
    public Mono<Map<String, String>> deleteProject(
            @PathVariable("projectName") String project_name) {
        log.info("DELETE /api/v1/projects/{}", project_name);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> project_service.deleteProjectByName(project_name, user_id))
                .thenReturn(Map.of("message", AppConstants.PROJECT_DELETED_SUCCESS_MESSAGE));
    }

    /**
     * Allows a user to join a project using project name, creator ID, and code.
     *
     * @param project_name the project name
     * @param creator_id the project creator's ID
     * @param code the project access code
     * @param member_pseudo the desired pseudo
     * @return Mono<MemberResponseDTO> the created member
     */
    @PostMapping(value = "/{projectName}/members/join",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Join project",
            description = "Join a project using project name, creator ID, and access code"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Successfully joined project",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid code or already a member"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            )
    })
    public Mono<MemberResponseDTO> joinProject(
            @PathVariable("projectName") String project_name,
            @RequestParam("creatorId") UUID creator_id,
            @RequestParam("code") @NotBlank @Size(min = PROJECT_CODE_LENGTH,
                    max = PROJECT_CODE_LENGTH) String code,
            @RequestParam("memberPseudo") @NotBlank @Size(max = MEMBER_PSEUDO_MAX_LENGTH)
            String member_pseudo) {
        log.info("POST /api/v1/projects/{}/members/join", project_name);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> project_service.joinProjectByName(
                        project_name, creator_id, code, member_pseudo, user_id));
    }

    /**
     * Retrieves all members of a project by name.
     *
     * @param project_name the project name
     * @return Flux<MemberResponseDTO> list of members
     */
    @GetMapping(value = "/{projectName}/members", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get project members",
            description = "Retrieves all members of a project by name"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Members retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            )
    })
    public Flux<MemberResponseDTO> getProjectMembers(
            @PathVariable("projectName") String project_name) {
        log.info("GET /api/v1/projects/{}/members", project_name);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMapMany(user_id -> project_service.getProjectMembersByName(
                        project_name, user_id));
    }

    /**
     * Retrieves a specific member's information.
     *
     * @param project_name the project name
     * @param member_id the member ID
     * @return Mono<MemberResponseDTO> the member information
     */
    @GetMapping(value = "/{projectName}/members/{memberId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get member details",
            description = "Retrieves detailed information about a specific member"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Member retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member or project not found"
            )
    })
    public Mono<MemberResponseDTO> getMemberById(
            @PathVariable("projectName") String project_name,
            @PathVariable("memberId") UUID member_id) {
        log.info("GET /api/v1/projects/{}/members/{}", project_name, member_id);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> {
                    // First get project to validate access, then get member
                    return project_service.getProjectDetailsByName(project_name, user_id)
                            .flatMap(project -> project_service.getMemberById(
                                    project.project_id(), member_id, user_id));
                });
    }

    /**
     * Updates a member's pseudo.
     * A member can only update their own pseudo.
     *
     * @param project_name the project name
     * @param member_id the member ID
     * @param new_pseudo the new pseudo
     * @return Mono<MemberResponseDTO> the updated member
     */
    @PatchMapping(value = "/{projectName}/members/{memberId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update member pseudo",
            description = "Updates a member's pseudo. Members can only update their own pseudo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Pseudo updated successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Pseudo already exists in project"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member not found"
            )
    })
    public Mono<MemberResponseDTO> updateMemberPseudo(
            @PathVariable("projectName") String project_name,
            @PathVariable("memberId") UUID member_id,
            @RequestParam("newPseudo") @NotBlank @Size(max = MEMBER_PSEUDO_MAX_LENGTH)
            String new_pseudo) {
        log.info("PATCH /api/v1/projects/{}/members/{}", project_name, member_id);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> {
                    return project_service.getProjectDetailsByName(project_name, user_id)
                            .flatMap(project -> project_service.updateMemberPseudo(
                                    project.project_id(), member_id, new_pseudo, user_id));
                });
    }

    /**
     * Removes a member from a project.
     * Only the project creator can remove members.
     *
     * @param project_name the project name
     * @param member_id the member ID to remove
     * @return Mono<Map> success message
     */
    @DeleteMapping(value = "/{projectName}/members/{memberId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Remove member",
            description = "Removes a member from the project. Only creator can remove members."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Member removed successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Unauthorized or attempting to remove creator"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member or project not found"
            )
    })
    public Mono<Map<String, String>> removeMember(
            @PathVariable("projectName") String project_name,
            @PathVariable("memberId") UUID member_id) {
        log.info("DELETE /api/v1/projects/{}/members/{}", project_name, member_id);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> {
                    return project_service.getProjectDetailsByName(project_name, user_id)
                            .flatMap(project -> project_service.removeMemberByName(
                                    project.project_name(), member_id, user_id));
                })
                .thenReturn(Map.of("message", MEMBER_REMOVED_SUCCESS_MESSAGE));
    }

    /**
     * Allows a user to leave a project they have joined.
     *
     * @param project_name the project name
     * @param creator_id the project creator's ID
     * @return Mono<Map> success message
     */
    @PostMapping(value = "/{projectName}/members/leave",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Leave project",
            description = "Leave a project. Creator cannot leave their own project."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Left project successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Creator cannot leave their own project"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            )
    })
    public Mono<Map<String, String>> leaveProject(
            @PathVariable("projectName") String project_name,
            @RequestParam("creatorId") UUID creator_id) {
        log.info("POST /api/v1/projects/{}/members/leave", project_name);
        return SecurityUtil.getAuthenticatedUserId()
                .flatMap(user_id -> project_service.leaveProjectByName(
                        project_name, creator_id, user_id))
                .thenReturn(Map.of("message", AppConstants.MEMBER_LEFT_SUCCESS_MESSAGE));
    }
}

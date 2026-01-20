package com.yowyob.feedback.controller;

import com.yowyob.feedback.dto.response.ProjectResponseDTO;
import com.yowyob.feedback.service.ProjectService;
import com.yowyob.feedback.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * REST controller for dashboard endpoints.
 * Provides user-centric views of their projects.
 *
 * Base path: /api/v1/dashboard
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "User dashboard API")
public class DashboardController {

    private final ProjectService project_service;

    /**
     * Retrieves all projects for the authenticated user.
     * Returns both created and joined projects.
     *
     * @return Flux<ProjectResponseDTO> list of all user's projects
     */
    @GetMapping(value = "/projects", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get all user projects",
            description = "Retrieves all projects (created and joined) for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public Flux<ProjectResponseDTO> getAllUserProjects() {
        log.info("GET /api/v1/dashboard/projects");
        return SecurityUtil.getAuthenticatedUserId()
                .flatMapMany(project_service::getAllUserProjects);
    }

    /**
     * Retrieves projects created by the authenticated user.
     *
     * @return Flux<ProjectResponseDTO> list of created projects
     */
    @GetMapping(value = "/projects/created", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get created projects",
            description = "Retrieves all projects created by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public Flux<ProjectResponseDTO> getCreatedProjects() {
        log.info("GET /api/v1/dashboard/projects/created");
        return SecurityUtil.getAuthenticatedUserId()
                .flatMapMany(project_service::getProjectsCreatedByUser);
    }

    /**
     * Retrieves projects joined by the authenticated user.
     *
     * @return Flux<ProjectResponseDTO> list of joined projects
     */
    @GetMapping(value = "/projects/joined", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Get joined projects",
            description = "Retrieves all projects joined by the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            )
    })
    public Flux<ProjectResponseDTO> getJoinedProjects() {
        log.info("GET /api/v1/dashboard/projects/joined");
        return SecurityUtil.getAuthenticatedUserId()
                .flatMapMany(project_service::getProjectsJoinedByUser);
    }
}

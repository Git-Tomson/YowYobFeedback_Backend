package com.yowyob.feedback.mapper;

import com.yowyob.feedback.dto.request.CreateProjectRequestDTO;
import com.yowyob.feedback.dto.response.ProjectDetailResponseDTO;
import com.yowyob.feedback.dto.response.ProjectResponseDTO;
import com.yowyob.feedback.entity.Project;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Manual mapper class for project entity conversions.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Component
public class ProjectMapper {

    /**
     * Maps a create project request DTO to a Project entity.
     *
     * @param request the create project request
     * @param creator_id the ID of the user creating the project
     * @param generated_code the generated project code
     * @return the Project entity
     */
    public Project toEntity(CreateProjectRequestDTO request, UUID creator_id,
                            String generated_code) {
        return Project.builder()
                .project_name(request.project_name())
                .code(generated_code)
                .description(request.description())
                .project_logo(request.project_logo())
                .creation_date_time(OffsetDateTime.now())
                .number_of_members(0)
                .creator_id(creator_id)
                .build();
    }

    /**
     * Maps a Project entity to a ProjectResponseDTO.
     *
     * @param project the Project entity
     * @return the ProjectResponseDTO
     */
    public ProjectResponseDTO toResponseDTO(Project project) {
        return ProjectResponseDTO.builder()
                .project_id(project.getProject_id())
                .project_name(project.getProject_name())
                .code(project.getCode())
                .description(project.getDescription())
                .project_logo(project.getProject_logo())
                .creation_date_time(project.getCreation_date_time())
                .number_of_members(project.getNumber_of_members())
                .creator_id(project.getCreator_id())
                .build();
    }

    /**
     * Maps a Project entity to a ProjectDetailResponseDTO.
     *
     * @param project the Project entity
     * @return the ProjectDetailResponseDTO builder (members to be added separately)
     */
    public ProjectDetailResponseDTO.ProjectDetailResponseDTOBuilder toDetailResponseDTOBuilder(
            Project project) {
        return ProjectDetailResponseDTO.builder()
                .project_id(project.getProject_id())
                .project_name(project.getProject_name())
                .code(project.getCode())
                .description(project.getDescription())
                .project_logo(project.getProject_logo())
                .creation_date_time(project.getCreation_date_time())
                .number_of_members(project.getNumber_of_members())
                .creator_id(project.getCreator_id());
    }
}

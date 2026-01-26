package com.yowyob.feedback.service;

import com.yowyob.feedback.dto.request.CreateFeedbackRequestDTO;
import com.yowyob.feedback.dto.request.UpdateFeedbackRequestDTO;
import com.yowyob.feedback.dto.response.FeedbackResponseDTO;
import com.yowyob.feedback.entity.Feedback;
import com.yowyob.feedback.entity.Member;
import com.yowyob.feedback.entity.Project;
import com.yowyob.feedback.mapper.FeedbackMapper;
import com.yowyob.feedback.repository.FeedbackRepository;
import com.yowyob.feedback.repository.MemberRepository;
import com.yowyob.feedback.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.yowyob.feedback.constant.AppConstants.FORBIDDEN_DELETE_FEEDBACK_MESSAGE;
import static com.yowyob.feedback.constant.AppConstants.FORBIDDEN_UPDATE_FEEDBACK_MESSAGE;

/**
 * Service class handling feedback operations.
 * Manages feedback creation, retrieval, update, and deletion.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final String FEEDBACK_NOT_FOUND_MESSAGE = "Feedback not found";
    private static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found";
    private static final String MEMBER_NOT_FOUND_MESSAGE = "Member not found in this project";
    private static final String FEEDBACK_CREATED_MESSAGE = "Feedback created successfully";
    private static final String FEEDBACK_UPDATED_MESSAGE = "Feedback updated successfully";
    private static final String FEEDBACK_DELETED_MESSAGE = "Feedback deleted successfully";

    private final FeedbackRepository feedback_repository;
    private final MemberRepository member_repository;
    private final ProjectRepository project_repository;
    private final FeedbackMapper feedback_mapper;
    private final JwtService jwt_service;

    /**
     * Creates a new feedback.
     *
     * @param request the feedback creation request
     * @return Mono<FeedbackResponseDTO>
     */
    @Transactional
    public Mono<FeedbackResponseDTO> createFeedback(CreateFeedbackRequestDTO request) {
        log.info("Creating feedback for project: {}, member: {}",
                request.project_id(), request.member_pseudo());

        return validateProjectExists(request.project_id())
                .then(findMemberByPseudoAndProject(request.member_pseudo(), request.project_id()))
                // We ensure that if the member does not exist, we throw an explicit error
                .switchIfEmpty(Mono.error(new RuntimeException("Member not found in this project")))
                .flatMap(member -> {
                    // We log to verify that the ID is present before insertion
                    log.debug("Found member ID: {}", member.getMember_id());

                    Feedback feedback = feedback_mapper.toEntity(request, member.getMember_id());
                    return feedback_repository.save(feedback);
                })
                .flatMap(this::buildResponseWithDetails)
                .doOnSuccess(response -> log.info("Feedback created: {}", response.feedback_id()))
                .doOnError(error -> log.error("Failed to create feedback: {}", error.getMessage()));
    }

    /**
     * Retrieves a specific feedback by ID.
     *
     * @param feedback_id the feedback ID
     * @return Mono<FeedbackResponseDTO>
     */
    public Mono<FeedbackResponseDTO> getFeedbackById(UUID feedback_id) {
        log.info("Retrieving feedback: {}", feedback_id);

        return feedback_repository.findById(feedback_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(FEEDBACK_NOT_FOUND_MESSAGE)))
                .flatMap(this::buildResponseWithDetails);
    }

    /**
     * Retrieves all feedbacks for a project.
     *
     * @param project_id the project ID
     * @return Flux<FeedbackResponseDTO>
     */
    public Flux<FeedbackResponseDTO> getFeedbacksByProject(UUID project_id) {
        log.info("Retrieving feedbacks for project: {}", project_id);

        return validateProjectExists(project_id)
                .thenMany(feedback_repository.findByProjectId(project_id))
                .flatMap(this::buildResponseWithDetails);
    }

    /**
     * Retrieves feedbacks by member pseudo within a project.
     *
     * @param member_pseudo the member pseudo
     * @param project_id the project ID
     * @return Flux<FeedbackResponseDTO>
     */
    public Flux<FeedbackResponseDTO> getFeedbacksByMemberAndProject(String member_pseudo,
                                                                    UUID project_id) {
        log.info("Retrieving feedbacks for member: {} in project: {}",
                member_pseudo, project_id);

        return feedback_repository.findByMemberPseudoAndProjectId(member_pseudo, project_id)
                .flatMap(this::buildResponseWithDetails);
    }

    /**
     * Retrieves all feedbacks from projects owned by a user.
     *
     * @param user_id the user ID
     * @return Flux<FeedbackResponseDTO>
     */
    public Flux<FeedbackResponseDTO> getFeedbacksByUserProjects(UUID user_id) {
        log.info("Retrieving feedbacks for user projects: {}", user_id);

        return feedback_repository.findByUserProjects(user_id)
                .flatMap(this::buildResponseWithDetails);
    }
    /**
     * Updates an existing feedback.
     * Only the member who created the feedback can update it.
     *
     * @param feedback_id the feedback ID
     * @param request the update request
     * @param authorization_header the Authorization header containing JWT token
     * @return Mono<FeedbackResponseDTO>
     * @throws IllegalArgumentException if user is not authorized or feedback not found
     */
    @Transactional
    public Mono<FeedbackResponseDTO> updateFeedback(UUID feedback_id,
                                                    UpdateFeedbackRequestDTO request,
                                                    String authorization_header) {
        log.info("Updating feedback: {}", feedback_id);

        return extractUserIdFromToken(authorization_header)
                .flatMap(current_user_id ->
                        feedback_repository.findById(feedback_id)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(FEEDBACK_NOT_FOUND_MESSAGE)))
                                .flatMap(feedback -> validateFeedbackOwnershipForUpdate(feedback, current_user_id))
                                .flatMap(feedback -> {
                                    feedback_mapper.updateEntity(feedback, request);
                                    return feedback_repository.save(feedback);
                                })
                                .flatMap(this::buildResponseWithDetails)
                )
                .doOnSuccess(response -> log.info("Feedback updated: {}", feedback_id))
                .doOnError(error -> log.error("Failed to update feedback: {}", feedback_id, error));
    }

    private Mono<Feedback> validateFeedbackOwnershipForUpdate(Feedback feedback, UUID current_user_id) {
        return member_repository.findById(feedback.getMember_id())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(MEMBER_NOT_FOUND_MESSAGE)))
                .flatMap(member -> {
                    if (!member.getUser_id().equals(current_user_id)) {
                        return Mono.error(new IllegalArgumentException(FORBIDDEN_UPDATE_FEEDBACK_MESSAGE));
                    }
                    return Mono.just(feedback);
                });
    }

    /**
     * Deletes a feedback.
     * Only the member who created the feedback can delete it.
     *
     * @param feedback_id the feedback ID
     * @param authorization_header the Authorization header containing JWT token
     * @return Mono<Void>
     * @throws IllegalArgumentException if user is not authorized or feedback not found
     */
    @Transactional
    public Mono<Void> deleteFeedback(UUID feedback_id, String authorization_header) {
        log.info("Deleting feedback: {}", feedback_id);

        return extractUserIdFromToken(authorization_header)
                .flatMap(current_user_id ->
                        feedback_repository.findById(feedback_id)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException(FEEDBACK_NOT_FOUND_MESSAGE)))
                                .flatMap(feedback -> validateFeedbackOwnership(feedback, current_user_id))
                                .flatMap(feedback -> feedback_repository.deleteById(feedback_id))
                )
                .doOnSuccess(v -> log.info("Feedback deleted: {}", feedback_id))
                .doOnError(error -> log.error("Failed to delete feedback: {}", feedback_id, error));
    }

    /**
     * Validates that the current user is the owner of the feedback.
     *
     * @param feedback the feedback to validate
     * @param current_user_id the current user ID from JWT token
     * @return Mono<Feedback> if validation succeeds
     * @throws IllegalArgumentException if user is not the owner
     */
    private Mono<Feedback> validateFeedbackOwnership(Feedback feedback, UUID current_user_id) {
        return member_repository.findById(feedback.getMember_id())
                .switchIfEmpty(Mono.error(new IllegalArgumentException(MEMBER_NOT_FOUND_MESSAGE)))
                .flatMap(member -> {
                    if (!member.getUser_id().equals(current_user_id)) {
                        return Mono.error(new IllegalArgumentException(FORBIDDEN_DELETE_FEEDBACK_MESSAGE));
                    }
                    return Mono.just(feedback);
                });
    }

    /**
     * Extracts user ID from JWT token.
     *
     * @param authorization_header the Authorization header
     * @return Mono<UUID> the user ID
     */
    private Mono<UUID> extractUserIdFromToken(String authorization_header) {
        if (authorization_header == null || !authorization_header.startsWith("Bearer ")) {
            return Mono.error(new IllegalArgumentException("Invalid authorization header"));
        }

        String token = authorization_header.substring("Bearer ".length());

        return jwt_service.validateTokenAndExtractUserId(token)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")));
    }
    private Mono<Void> validateProjectExists(UUID project_id) {
        return project_repository.findById(project_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(PROJECT_NOT_FOUND_MESSAGE)))
                .then();
    }

    private Mono<Member> findMemberByPseudoAndProject(String member_pseudo, UUID project_id) {
        return member_repository.findByMemberPseudoAndProjectId(member_pseudo, project_id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException(MEMBER_NOT_FOUND_MESSAGE)));
    }

    private Mono<FeedbackResponseDTO> buildResponseWithDetails(Feedback feedback) {
        Mono<String> project_name = project_repository.findById(feedback.getTarget_project_id())
                .map(Project::getProject_name)
                .defaultIfEmpty("Unknown Project");

        Mono<String> member_pseudo = member_repository.findById(feedback.getMember_id())
                .map(Member::getMember_pseudo)
                .defaultIfEmpty("Unknown Member");

        return Mono.zip(project_name, member_pseudo)
                .map(tuple -> feedback_mapper.toResponseDTOWithDetails(
                        feedback, tuple.getT1(), tuple.getT2()));
    }
}

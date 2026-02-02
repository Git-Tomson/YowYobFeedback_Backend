package com.yowyob.feedback.mapper;

import com.yowyob.feedback.dto.request.CreateFeedbackRequestDTO;
import com.yowyob.feedback.dto.request.UpdateFeedbackRequestDTO;
import com.yowyob.feedback.dto.response.FeedbackResponseDTO;
import com.yowyob.feedback.entity.Feedback;
import com.yowyob.feedback.repository.FeedbackWithDetails;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;

/**
 * Manual mapper class for feedback entity conversions.
 * Handles conversion between entities and DTOs for feedbacks.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Component
public class FeedbackMapper {

    /**
     * Maps CreateFeedbackRequestDTO to Feedback entity.
     *
     * @param request the creation request
     * @param member_id the member ID
     * @return Feedback entity
     */
    public Feedback toEntity(CreateFeedbackRequestDTO request, java.util.UUID member_id) {
        return Feedback.builder()
                .content(request.content())
                .attachments(request.attachments() != null ? request.attachments() : Collections.emptyList())
                .target_project_id(request.project_id())
                .member_id(member_id)
                .feedback_date_time(OffsetDateTime.now())
                .number_of_likes(0)
                .number_of_comments(0)
                .build();
    }

    /**
     * Maps Feedback entity to FeedbackResponseDTO.
     *
     * @param feedback the feedback entity
     * @return FeedbackResponseDTO
     */
    public FeedbackResponseDTO toFeedbackResponseDTO(Feedback feedback) {
        return FeedbackResponseDTO.builder()
                .feedback_id(feedback.getFeedback_id())
                .feedback_date_time(feedback.getFeedback_date_time())
                .content(feedback.getContent())
                .attachments(feedback.getAttachments())
                .target_project_id(feedback.getTarget_project_id())
                .member_id(feedback.getMember_id())
                .number_of_likes(feedback.getNumber_of_likes())
                .number_of_comments(feedback.getNumber_of_comments())
                .build();
    }
    /**
     * Maps FeedbackWithDetails projection to FeedbackResponseDTO.
     * Converts projection interface containing all enriched feedback data to response DTO.
     *
     * @param feedbackWithDetails the feedback projection with member and project details
     * @return FeedbackResponseDTO
     */
    public FeedbackResponseDTO toFeedbackResponseDTO(FeedbackWithDetails feedbackWithDetails) {
        return FeedbackResponseDTO.builder()
                .feedback_id(feedbackWithDetails.getFeedbackId())
                .feedback_date_time(feedbackWithDetails.getFeedbackDateTime())
                .content(feedbackWithDetails.getContent())
                .attachments(feedbackWithDetails.getAttachments() != null ? java.util.Arrays.asList(feedbackWithDetails.getAttachments()) : Collections.emptyList())
                .target_project_id(feedbackWithDetails.getTargetProjectId())
                .project_name(feedbackWithDetails.getProjectName())
                .member_id(feedbackWithDetails.getMemberId())
                .member_pseudo(feedbackWithDetails.getMemberPseudo())
                .number_of_likes(feedbackWithDetails.getNumberOfLikes())
                .number_of_comments(feedbackWithDetails.getNumberOfComments())
                .build();
    }

    /**
     * Maps Feedback entity to FeedbackResponseDTO with additional details.
     *
     * @param feedback the feedback entity
     * @param project_name the project name
     * @param member_pseudo the member pseudo
     * @return FeedbackResponseDTO
     */
    public FeedbackResponseDTO toResponseDTOWithDetails(Feedback feedback, String project_name,
                                                        String member_pseudo) {
        return FeedbackResponseDTO.builder()
                .feedback_id(feedback.getFeedback_id())
                .feedback_date_time(feedback.getFeedback_date_time())
                .content(feedback.getContent())
                .attachments(feedback.getAttachments())
                .target_project_id(feedback.getTarget_project_id())
                .project_name(project_name)
                .member_id(feedback.getMember_id())
                .member_pseudo(member_pseudo)
                .number_of_likes(feedback.getNumber_of_likes())
                .number_of_comments(feedback.getNumber_of_comments())
                .build();
    }

    /**
     * Updates feedback entity with data from UpdateFeedbackRequestDTO.
     *
     * @param feedback the existing feedback
     * @param request the update request
     */
    public void updateEntity(Feedback feedback, UpdateFeedbackRequestDTO request) {
        feedback.setContent(request.content());
        if (request.attachments() != null) {
            feedback.setAttachments(request.attachments());
        }
    }
}

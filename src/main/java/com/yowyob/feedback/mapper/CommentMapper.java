package com.yowyob.feedback.mapper;

import com.yowyob.feedback.dto.request.CreateCommentRequestDTO;
import com.yowyob.feedback.dto.request.UpdateCommentRequestDTO;
import com.yowyob.feedback.dto.response.CommentResponseDTO;
import com.yowyob.feedback.entity.Comment;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Manual mapper class for comment entity conversions.
 * Handles conversion between entities and DTOs for comments.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Component
public class CommentMapper {

    /**
     * Maps CreateCommentRequestDTO to Comment entity.
     *
     * @param request the creation request
     * @return Comment entity
     */
    public Comment toEntity(CreateCommentRequestDTO request) {
        return Comment.builder()
                .feedback_id(request.feedback_id())
                .commenter_id(request.commenter_id())
                .content(request.content())
                .comments_date_time(OffsetDateTime.now())
                .number_of_likes(0)
                .build();
    }

    /**
     * Maps Comment entity to CommentResponseDTO.
     *
     * @param comment the comment entity
     * @return CommentResponseDTO
     */
    public CommentResponseDTO toResponseDTO(Comment comment) {
        return CommentResponseDTO.builder()
                .comments_id(comment.getComments_id())
                .feedback_id(comment.getFeedback_id())
                .commenter_id(comment.getCommenter_id())
                .content(comment.getContent())
                .comments_date_time(comment.getComments_date_time())
                .number_of_likes(comment.getNumber_of_likes())
                .build();
    }

    /**
     * Maps Comment entity to CommentResponseDTO with commenter name.
     *
     * @param comment the comment entity
     * @param commenter_name the commenter name
     * @return CommentResponseDTO
     */
    public CommentResponseDTO toResponseDTOWithName(Comment comment, String commenter_name) {
        return CommentResponseDTO.builder()
                .comments_id(comment.getComments_id())
                .feedback_id(comment.getFeedback_id())
                .commenter_id(comment.getCommenter_id())
                .commenter_name(commenter_name)
                .content(comment.getContent())
                .comments_date_time(comment.getComments_date_time())
                .number_of_likes(comment.getNumber_of_likes())
                .build();
    }
    /**
     * Updates comment entity with data from UpdateCommentRequestDTO.
     *
     * @param comment the existing comment
     * @param request the update request
     */
    public void updateEntity(Comment comment, UpdateCommentRequestDTO request) {
        comment.setContent(request.content());
    }
}

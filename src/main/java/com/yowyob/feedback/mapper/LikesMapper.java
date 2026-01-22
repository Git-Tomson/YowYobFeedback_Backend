package com.yowyob.feedback.mapper;

import com.yowyob.feedback.dto.request.CreateLikeRequestDTO;
import com.yowyob.feedback.dto.response.LikeResponseDTO;
import com.yowyob.feedback.entity.Likes;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Manual mapper class for like entity conversions.
 * Handles conversion between entities and DTOs for likes.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Component
public class LikesMapper {

    /**
     * Maps CreateLikeRequestDTO to Like entity.
     *
     * @param request the creation request
     * @param liker_id the liker ID from JWT token
     * @return Like entity
     */
    public Likes toEntity(CreateLikeRequestDTO request, UUID liker_id) {
        return Likes.builder()
                .feedback_id(request.feedback_id())
                .liker_id(liker_id)
                .likes_date_time(OffsetDateTime.now())
                .build();
    }

    /**
     * Maps Like entity to LikeResponseDTO.
     *
     * @param likes the like entity
     * @return LikeResponseDTO
     */
    public LikeResponseDTO toResponseDTO(Likes likes) {
        return LikeResponseDTO.builder()
                .feedback_id(likes.getFeedback_id())
                .liker_id(likes.getLiker_id())
                .likes_date_time(likes.getLikes_date_time())
                .build();
    }

    /**
     * Maps Like entity to LikeResponseDTO with liker name.
     *
     * @param likes the like entity
     * @param liker_name the liker name
     * @return LikeResponseDTO
     */
    public LikeResponseDTO toResponseDTOWithName(Likes likes, String liker_name) {
        return LikeResponseDTO.builder()
                .feedback_id(likes.getFeedback_id())
                .liker_id(likes.getLiker_id())
                .liker_name(liker_name)
                .likes_date_time(likes.getLikes_date_time())
                .build();
    }
}

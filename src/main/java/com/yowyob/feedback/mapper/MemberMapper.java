package com.yowyob.feedback.mapper;

import com.yowyob.feedback.dto.request.JoinProjectRequestDTO;
import com.yowyob.feedback.dto.response.MemberResponseDTO;
import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.entity.Member;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Manual mapper class for member entity conversions.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Component
public class MemberMapper {

    /**
     * Maps a join project request DTO to a Member entity.
     *
     * @param request the join project request
     * @param user_id the ID of the user joining
     * @return the Member entity
     */
    public Member toEntity(JoinProjectRequestDTO request, UUID user_id) {
        return Member.builder()
                .member_pseudo(request.member_pseudo())
                .user_id(user_id)
                .project_id(request.project_id())
                .build();
    }

    /**
     * Maps a Member entity to a MemberResponseDTO.
     *
     * @param member the Member entity
     * @return the MemberResponseDTO
     */
    public MemberResponseDTO toResponseDTO(Member member) {
        return MemberResponseDTO.builder()
                .member_id(member.getMember_id())
                .member_pseudo(member.getMember_pseudo())
                .user_id(member.getUser_id())
                .project_id(member.getProject_id())
                .build();
    }

    /**
     * Maps a Member entity with AppUser information to a complete MemberResponseDTO.
     *
     * @param member the Member entity
     * @param user the AppUser entity
     * @return the complete MemberResponseDTO
     */
    public MemberResponseDTO toResponseDTO(Member member, AppUser user) {
        return MemberResponseDTO.builder()
                .member_id(member.getMember_id())
                .member_pseudo(member.getMember_pseudo())
                .user_id(member.getUser_id())
                .project_id(member.getProject_id())
                .user_firstname(user.getUser_firstname())
                .user_lastname(user.getUser_lastname())
                .user_logo(user.getUser_logo())
                .build();
    }
}

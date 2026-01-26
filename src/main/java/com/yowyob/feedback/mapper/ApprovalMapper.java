package com.yowyob.feedback.mapper;

import com.yowyob.feedback.dto.request.CreateApprovalRequestDTO;
import com.yowyob.feedback.dto.response.ApprovalResponseDTO;
import com.yowyob.feedback.entity.Approval;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Manual mapper class for approval entity conversions.
 * Handles conversion between entities and DTOs for approvals.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Component
public class ApprovalMapper {

    /**
     * Maps CreateApprovalRequestDTO to Approval entity.
     *
     * @param request the creation request
     * @param approver_id the approver ID from JWT token
     * @return Approval entity
     */
    public Approval toEntity(CreateApprovalRequestDTO request, UUID approver_id) {
        return Approval.builder()
                .comments_id(request.comments_id())
                .approver_id(approver_id)
                .approval_date_time(OffsetDateTime.now())
                .build();
    }

    /**
     * Maps Approval entity to ApprovalResponseDTO.
     *
     * @param approval the approval entity
     * @return ApprovalResponseDTO
     */
    public ApprovalResponseDTO toResponseDTO(Approval approval) {
        return ApprovalResponseDTO.builder()
                .comments_id(approval.getComments_id())
                .approver_id(approval.getApprover_id())
                .approval_date_time(approval.getApproval_date_time())
                .build();
    }

    /**
     * Maps Approval entity to ApprovalResponseDTO with approver name.
     *
     * @param approval the approval entity
     * @param approver_name the approver name
     * @return ApprovalResponseDTO
     */
    public ApprovalResponseDTO toResponseDTOWithName(Approval approval, String approver_name) {
        return ApprovalResponseDTO.builder()
                .comments_id(approval.getComments_id())
                .approver_id(approval.getApprover_id())
                .approver_name(approver_name)
                .approval_date_time(approval.getApproval_date_time())
                .build();
    }
}

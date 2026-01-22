package com.yowyob.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing an approval (like) on a comment.
 * A user can approve a comment only once.
 * Composite primary key: (comments_id, approver_id)
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("approval")
public class Approval {

    @Column("comments_id")
    private UUID comments_id;

    @Column("approver_id")
    private UUID approver_id;

    @Column("approval_date_time")
    private OffsetDateTime approval_date_time;
}

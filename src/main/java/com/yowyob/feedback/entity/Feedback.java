package com.yowyob.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a feedback in the system.
 * Feedbacks are submitted by members within a project.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("feedback")
public class Feedback {

    @Id
    @Column("feedback_id")
    private UUID feedback_id;

    @Column("feedback_date_time")
    private OffsetDateTime feedback_date_time;

    @Column("content")
    private String content;

    @Column("attachments")
    private List<String> attachments;

    @Column("target_project_id")
    private UUID target_project_id;

    @Column("member_id")
    private UUID member_id;

    @Column("number_of_likes")
    private Integer number_of_likes;

    @Column("number_of_comments")
    private Integer number_of_comments;
}

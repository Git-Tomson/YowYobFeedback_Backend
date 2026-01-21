package com.yowyob.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing a comment on a feedback.
 * Comments can be made by any user in the system.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-20
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("comments")
public class Comment {

    @Id
    @Column("comments_id")
    private UUID comments_id;

    @Column("feedback_id")
    private UUID feedback_id;

    @Column("commenter_id")
    private UUID commenter_id;

    @Column("content")
    private String content;

    @Column("comments_date_time")
    private OffsetDateTime comments_date_time;

    @Column("number_of_likes")
    private Integer number_of_likes;
}

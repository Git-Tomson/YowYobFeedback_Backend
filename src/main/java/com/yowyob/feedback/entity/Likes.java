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
 * Entity representing a like on a feedback.
 * A user can like a feedback only once.
 * Composite primary key: (feedback_id, liker_id)
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-21
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("likes")
public class Likes {

    @Column("feedback_id")
    private UUID feedback_id;

    @Column("liker_id")
    private UUID liker_id;

    @Column("likes_date_time")
    private OffsetDateTime likes_date_time;
}

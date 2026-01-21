package com.yowyob.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Entity representing a member in a project.
 * A member is an AppUser who has joined a specific project.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("member")
public class Member implements Persistable<UUID> {

    @Id
    @Column("member_id")
    private UUID member_id;

    @Column("member_pseudo")
    private String member_pseudo;

    @Column("user_id")
    private UUID user_id;

    @Column("project_id")
    private UUID project_id;

    @Builder.Default @Transient
    private boolean is_new = false;

    @Override
    public UUID getId() {
        return member_id;
    }

    @Override
    public boolean isNew() {
        return is_new || member_id == null;
    }

    public void setNew(boolean is_new) {
        this.is_new = is_new;
    }
}

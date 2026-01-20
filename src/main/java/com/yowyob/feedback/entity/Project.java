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

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entity representing a project in the system.
 * Projects are created by users and can have multiple members.
 *
 * @author Thomas Djotio Ndié
 * @since 2025-01-19
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("project")
public class Project implements Persistable<UUID> {

    @Id
    @Column("project_id")
    private UUID project_id;

    @Column("project_name")
    private String project_name;

    @Column("code")
    private String code;

    @Column("description")
    private String description;

    @Column("project_logo")
    private String project_logo;

    @Column("creation_date_time")
    private OffsetDateTime creation_date_time;

    @Column("number_of_members")
    private Integer number_of_members;

    @Column("creator_id")
    private UUID creator_id;

    @Transient
    private boolean is_new = false;

    @Override
    public UUID getId() {
        return project_id;
    }

    @Override
    public boolean isNew() {
        return is_new || project_id == null;
    }

    public void setNew(boolean is_new) {
        this.is_new = is_new;
    }
}

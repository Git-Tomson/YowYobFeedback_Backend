package com.yowyob.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Entity representing an organization user.
 * Extends AppUser with organization-specific attributes.
 *
 * This entity uses joined table inheritance pattern where:
 * - Common attributes are in app_user table
 * - Organization-specific attributes are in organization table
 * - Both tables share the same primary key (org_id = user_id)
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-12
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table("organization")
public class Organization implements Persistable<UUID> {

    @Id
    @Column("org_id")
    private UUID org_id;

    @Column("location")
    private String location;

    @Builder.Default @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return org_id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}

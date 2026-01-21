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
 * Entity representing a person user.
 * Extends AppUser with person-specific attributes.
 *
 * This entity uses joined table inheritance pattern where:
 * - Common attributes are in app_user table
 * - Person-specific attributes are in person table
 * - Both tables share the same primary key (person_id = user_id)
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
@Table("person")
public class Person implements Persistable<UUID> {

    @Id
    @Column("person_id")
    private UUID person_id;

    @Column("occupation")
    private String occupation;

    @Builder.Default @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return person_id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}

package com.yowyob.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base entity representing an application user.
 * This is the superclass for Person and Organization entities.
 *
 * This entity maps to the app_user table which stores common attributes
 * for all users. Specific attributes are stored in person or organization tables.
 *
 * A user can authenticate using either email or contact number.
 * At least one of these fields must be provided.
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
@Table("app_user")
public class AppUser {

    @Id
    @Column("user_id")
    private UUID user_id;

    @Column("user_type")
    private UserType user_type;

    @Column("user_firstname")
    private String user_firstname;

    @Column("user_lastname")
    private String user_lastname;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("contact")
    private String contact;

    @Column("user_logo")
    private String user_logo;

    @Column("domain")
    private String domain;

    @Column("description")
    private String description;

    @Column("registration_date_time")
    private OffsetDateTime registration_date_time;

    @Column("certified")
    private Boolean certified;

    @Column("two_fa_enabled")
    private Boolean two_fa_enabled;

    @Column("two_fa_secret")
    private String two_fa_secret;

    @Column("two_fa_backup_codes")
    private String[] two_fa_backup_codes;
}

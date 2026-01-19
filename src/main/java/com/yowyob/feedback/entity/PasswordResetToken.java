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
 * Entity representing password reset tokens.
 * Used for secure password recovery functionality.
 *
 * @author Thomas Djotio Ndié
 * @since 2024-12-30
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("password_reset_token")
public class PasswordResetToken {

    @Id
    @Column("token_id")
    private UUID token_id;

    @Column("token")
    private String token;

    @Column("user_id")
    private UUID user_id;

    @Column("expiry_date")
    private OffsetDateTime expiry_date;

    @Column("used")
    private Boolean used;

    @Column("created_at")
    private OffsetDateTime created_at;
}

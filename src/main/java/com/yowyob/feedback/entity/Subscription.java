package com.yowyob.feedback.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("subscription")
public class Subscription {

    @Column("followed_id")
    private UUID followed_id;

    @Column("follower_id")
    private UUID follower_id;

    @Column("follow_date_time")
    private OffsetDateTime follow_date_time;
}
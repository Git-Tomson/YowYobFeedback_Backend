package com.yowyob.feedback.dto.response;

import lombok.Data;

@Data
public class SubscriptionStatsDTO {
    private Long followingCount;   // Nombre de personnes que je suis
    private Long followersCount;   // Nombre de personnes qui me suivent

    public Long getFollowingCount() { return followingCount; }
    public void setFollowingCount(Long followingCount) { this.followingCount = followingCount; }
    public Long getFollowersCount() { return followersCount; }
    public void setFollowersCount(Long followersCount) { this.followersCount = followersCount; }
}
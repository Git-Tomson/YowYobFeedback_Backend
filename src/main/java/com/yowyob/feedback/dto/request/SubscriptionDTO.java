package com.yowyob.feedback.dto.request;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SubscriptionDTO {
    private UUID followedId;
    private UUID followerId;
    private PersonDTO followed;
    private PersonDTO follower;
    private LocalDateTime followDateTime;

    public UUID getFollowedId() { return followedId; }
    public void setFollowedId(UUID followedId) { this.followedId = followedId; }
    public UUID getFollowerId() { return followerId; }
    public void setFollowerId(UUID followerId) { this.followerId = followerId; }
    public PersonDTO getFollowed() { return followed; }
    public void setFollowed(PersonDTO followed) { this.followed = followed; }
    public PersonDTO getFollower() { return follower; }
    public void setFollower(PersonDTO follower) { this.follower = follower; }
    public LocalDateTime getFollowDateTime() { return followDateTime; }
    public void setFollowDateTime(LocalDateTime followDateTime) { this.followDateTime = followDateTime; }
}

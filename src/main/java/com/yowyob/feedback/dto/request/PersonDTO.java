package com.yowyob.feedback.dto.request;

import com.yowyob.feedback.dto.response.SubscriptionStatsDTO;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PersonDTO {
    private UUID userId; // Changed to UUID to match expected type in Service
    private String firstName;
    private String lastName;
    private String email;
    private String contact;
    private String profileImage;
    private String domain;
    private String description;
    private Boolean certified;
    private String userType;
    private LocalDateTime registrationDateTime;
    private SubscriptionStatsDTO subscriptionStats;
    private String occupation; // Added missing field

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getCertified() { return certified; }
    public void setCertified(Boolean certified) { this.certified = certified; }
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }
    public LocalDateTime getRegistrationDateTime() { return registrationDateTime; }
    public void setRegistrationDateTime(LocalDateTime registrationDateTime) { this.registrationDateTime = registrationDateTime; }
    public SubscriptionStatsDTO getSubscriptionStats() { return subscriptionStats; }
    public void setSubscriptionStats(SubscriptionStatsDTO subscriptionStats) { this.subscriptionStats = subscriptionStats; }
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
}
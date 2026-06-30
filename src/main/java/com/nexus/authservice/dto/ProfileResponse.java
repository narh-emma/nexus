package com.nexus.authservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProfileResponse {

    private String id;
    private String email;
    private String fullName;
    private String indexNumber;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String bio;
    private String profilePictureUrl;
    private String preferredLanguage;
    private String signDialect;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===== CONSTRUCTORS =====
    public ProfileResponse() {}

    public ProfileResponse(String id, String email, String fullName, String indexNumber,
                           String phoneNumber, LocalDate dateOfBirth, String bio,
                           String profilePictureUrl, String preferredLanguage,
                           String signDialect, String role, LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.indexNumber = indexNumber;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.bio = bio;
        this.profilePictureUrl = profilePictureUrl;
        this.preferredLanguage = preferredLanguage;
        this.signDialect = signDialect;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ===== GETTERS =====
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getIndexNumber() { return indexNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getBio() { return bio; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public String getSignDialect() { return signDialect; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // ===== SETTERS =====
    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setIndexNumber(String indexNumber) { this.indexNumber = indexNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setBio(String bio) { this.bio = bio; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public void setSignDialect(String signDialect) { this.signDialect = signDialect; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
package com.nexus.authservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String indexNumber;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    private String preferredLanguage = "en";
    private String signDialect = "ASL";
    
    @Column(columnDefinition = "TEXT")
    private String medicalPreferences;
    
    // ===== ROLE FIELD =====
    @Column(name = "role")
    private String role = "USER";
    
    // ===== PROFILE FIELDS - ADD THESE =====
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public User() {}
    
    // ===== GETTERS =====
    public UUID getId() { return id; }
    public String getIndexNumber() { return indexNumber; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public String getSignDialect() { return signDialect; }
    public String getMedicalPreferences() { return medicalPreferences; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // ===== PROFILE GETTERS =====
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getBio() { return bio; }
    
    // ===== SETTERS =====
    public void setId(UUID id) { this.id = id; }
    public void setIndexNumber(String indexNumber) { this.indexNumber = indexNumber; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public void setSignDialect(String signDialect) { this.signDialect = signDialect; }
    public void setMedicalPreferences(String medicalPreferences) { this.medicalPreferences = medicalPreferences; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // ===== PROFILE SETTERS =====
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setBio(String bio) { this.bio = bio; }
    
    // ===== HELPER METHOD =====
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
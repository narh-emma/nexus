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
    
    // ===== PROFILE FIELDS =====
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    // ===== EMAIL VERIFICATION FIELDS =====
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "pending_email")
    private String pendingEmail;
    
    // ===== PRIVACY & CONSENT FIELDS =====
    @Column(name = "data_consent")
    private boolean dataConsent = false;
    
    @Column(name = "share_medical_data")
    private boolean shareMedicalData = false;
    
    @Column(name = "share_location")
    private boolean shareLocation = false;
    
    @Column(name = "receive_notifications")
    private boolean receiveNotifications = true;
    
    @Column(name = "consent_given_at")
    private LocalDateTime consentGivenAt;
    
    @Column(name = "consent_version")
    private String consentVersion = "1.0";
    
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

    // ===== EMAIL VERIFICATION GETTERS =====
    public boolean isEmailVerified() { return emailVerified; }
    public String getPendingEmail() { return pendingEmail; }
    
    // ===== PRIVACY GETTERS =====
    public boolean isDataConsent() { return dataConsent; }
    public boolean isShareMedicalData() { return shareMedicalData; }
    public boolean isShareLocation() { return shareLocation; }
    public boolean isReceiveNotifications() { return receiveNotifications; }
    public LocalDateTime getConsentGivenAt() { return consentGivenAt; }
    public String getConsentVersion() { return consentVersion; }
    
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

    // ===== EMAIL VERIFICATION SETTERS =====
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public void setPendingEmail(String pendingEmail) { this.pendingEmail = pendingEmail; }
    
    // ===== PRIVACY SETTERS =====
    public void setDataConsent(boolean dataConsent) { this.dataConsent = dataConsent; }
    public void setShareMedicalData(boolean shareMedicalData) { this.shareMedicalData = shareMedicalData; }
    public void setShareLocation(boolean shareLocation) { this.shareLocation = shareLocation; }
    public void setReceiveNotifications(boolean receiveNotifications) { this.receiveNotifications = receiveNotifications; }
    public void setConsentGivenAt(LocalDateTime consentGivenAt) { this.consentGivenAt = consentGivenAt; }
    public void setConsentVersion(String consentVersion) { this.consentVersion = consentVersion; }
    
    // ===== HELPER METHOD =====
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
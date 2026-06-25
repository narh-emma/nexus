package com.nexus.authservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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
    
    // ===== ADD THIS: OPTIONAL ROLE FIELD =====
    @Column(name = "role")
    private String role = "USER";  // Can be null for regular users, "ADMIN" for admins
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public User() {}
    
    // Getters
    public UUID getId() { return id; }
    public String getIndexNumber() { return indexNumber; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public String getSignDialect() { return signDialect; }
    public String getMedicalPreferences() { return medicalPreferences; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    
    // ===== ADD GETTER AND SETTER FOR ROLE =====
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    // ===== ADD HELPER METHOD TO CHECK IF ADMIN =====
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setIndexNumber(String indexNumber) { this.indexNumber = indexNumber; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public void setSignDialect(String signDialect) { this.signDialect = signDialect; }
    public void setMedicalPreferences(String medicalPreferences) { this.medicalPreferences = medicalPreferences; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
package com.nexus.authservice.dto;

import jakarta.validation.constraints.Email;
import java.time.LocalDate;

public class ProfileRequest {

    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String bio;
    private String preferredLanguage;
    private String signDialect;

    // ===== CONSTRUCTORS =====
    public ProfileRequest() {}

    public ProfileRequest(String fullName, String email, String phoneNumber, 
                          LocalDate dateOfBirth, String bio, String preferredLanguage, 
                          String signDialect) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.bio = bio;
        this.preferredLanguage = preferredLanguage;
        this.signDialect = signDialect;
    }

    // ===== GETTERS =====
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getBio() { return bio; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public String getSignDialect() { return signDialect; }

    // ===== SETTERS =====
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setBio(String bio) { this.bio = bio; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public void setSignDialect(String signDialect) { this.signDialect = signDialect; }
}
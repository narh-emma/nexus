package com.nexus.authservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "token_blacklist")
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "token", columnDefinition = "TEXT", nullable = false)
    private String token;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public TokenBlacklist() {}

    public TokenBlacklist(String token, String userEmail, LocalDateTime expiresAt) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public UUID getId() { return id; }
    public String getToken() { return token; }
    public String getUserEmail() { return userEmail; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setToken(String token) { this.token = token; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
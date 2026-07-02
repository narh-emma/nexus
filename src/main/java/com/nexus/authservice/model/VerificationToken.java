package com.nexus.authservice.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_tokens")
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UUID userId;

    /**
     * REGISTRATION: verifies the user's existing email at signup.
     * EMAIL_CHANGE: verifies a NEW email the user wants to switch to
     *               (pendingEmail on the User isn't applied until this is confirmed).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;

    /** Only used for EMAIL_CHANGE tokens -- the new address awaiting confirmation. */
    private String pendingEmail;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used = false;

    public enum TokenType { REGISTRATION, EMAIL_CHANGE }

    public VerificationToken() {}

    public VerificationToken(String token, UUID userId, TokenType type, String pendingEmail, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = userId;
        this.type = type;
        this.pendingEmail = pendingEmail;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // ==================== GETTERS/SETTERS ====================

    public UUID getId() { return id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public TokenType getType() { return type; }
    public void setType(TokenType type) { this.type = type; }
    public String getPendingEmail() { return pendingEmail; }
    public void setPendingEmail(String pendingEmail) { this.pendingEmail = pendingEmail; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }
}

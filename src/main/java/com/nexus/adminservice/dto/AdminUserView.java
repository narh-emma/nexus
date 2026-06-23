package com.nexus.adminservice.dto;

import java.time.Instant;

/**
 * Read-only projection of a user, returned to the admin console.
 * No password hash or refresh tokens ever leave the Auth Service schema.
 */
public class AdminUserView {

    private String id;
    private String email;
    private String role;
    private boolean enabled;
    private Instant createdAt;

    public AdminUserView() {}

    public AdminUserView(String id, String email, String role, boolean enabled, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

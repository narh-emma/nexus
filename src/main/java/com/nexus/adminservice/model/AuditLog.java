package com.nexus.adminservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {
    
    @Id
    private String id;  // Using String as ID
    
    @Column(nullable = false)
    private String actorId;
    
    @Column(nullable = false)
    private String action;
    
    private String targetId;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public AuditLog() {}
    
    public AuditLog(String id, String actorId, String action, String targetId, String details) {
        this.id = id;
        this.actorId = actorId;
        this.action = action;
        this.targetId = targetId;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
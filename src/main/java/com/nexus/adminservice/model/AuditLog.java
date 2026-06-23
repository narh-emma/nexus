package com.nexus.adminservice.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * admin_db.audit_log
 *
 * Records every admin mutation (per Backend Plan §7: "All admin mutations
 * ... written to log tables"). Stores user_id as a plain reference column,
 * never a hard foreign key, per the microservice rule in §4.2.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "actor_id", nullable = false)
    private String actorId;        // admin user id who performed the action

    @Column(name = "target_id")
    private String targetId;       // affected user id, if any

    @Column(nullable = false)
    private String action;         // e.g. "USER_DISABLED", "USER_ENABLED", "ROLE_CHANGED"

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public AuditLog() {}

    public AuditLog(String actorId, String targetId, String action, String details) {
        this.actorId = actorId;
        this.targetId = targetId;
        this.action = action;
        this.details = details;
    }

    public String getId() { return id; }
    public String getActorId() { return actorId; }
    public String getTargetId() { return targetId; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public Instant getCreatedAt() { return createdAt; }
}

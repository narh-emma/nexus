package com.nexus.adminservice.repository;

import com.nexus.adminservice.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
    
    // ===== Spring Data JPA method naming (auto-implemented) =====
    
    /**
     * Find audit logs by action
     */
    Page<AuditLog> findByAction(String action, Pageable pageable);
    
    /**
     * Find audit logs by actor ID
     */
    Page<AuditLog> findByActorId(String actorId, Pageable pageable);
    
    /**
     * Find audit logs by action AND actor ID
     */
    Page<AuditLog> findByActionAndActorId(String action, String actorId, Pageable pageable);
    
    // ===== Custom queries with @Query (optional, for more complex searches) =====
    
    /**
     * Search audit logs with optional filters
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:action IS NULL OR :action = '' OR a.action = :action) AND " +
           "(:actorId IS NULL OR :actorId = '' OR a.actorId = :actorId)")
    Page<AuditLog> searchAuditLogs(@Param("action") String action, 
                                   @Param("actorId") String actorId, 
                                   Pageable pageable);
}
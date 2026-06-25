package com.nexus.adminservice.service;

import com.nexus.adminservice.dto.UserManagementDTO;
import com.nexus.adminservice.model.AuditLog;
import com.nexus.adminservice.repository.AuditLogRepository;
import com.nexus.authservice.model.User;
import com.nexus.authservice.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== AUDIT LOG METHODS ====================
    
    public Page<AuditLog> getAuditLogs(String action, String actorId, Pageable pageable) {
        if (action != null && actorId != null) {
            return auditLogRepository.findByActionAndActorId(action, actorId, pageable);
        } else if (action != null) {
            return auditLogRepository.findByAction(action, pageable);
        } else if (actorId != null) {
            return auditLogRepository.findByActorId(actorId, pageable);
        }
        return auditLogRepository.findAll(pageable);
    }

    // FIX: Change parameter from UUID to String
    public AuditLog getAuditLogById(String id) {
        return auditLogRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Audit log not found"));
    }

    public Page<AuditLog> getUserAuditLogs(String userId, Pageable pageable) {
        return auditLogRepository.findByActorId(userId, pageable);
    }

    // ==================== USER MANAGEMENT METHODS ====================
    
    public Page<UserManagementDTO> getAllUsers(String search, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            return userRepository.searchUsers(search, pageable)
                .map(this::convertToDTO);
        }
        return userRepository.findAll(pageable)
            .map(this::convertToDTO);
    }

    public UserManagementDTO getUserById(String userId) {
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    public UserManagementDTO updateUserRole(String userId, String role) {
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    public UserManagementDTO updateUserStatus(String userId, boolean enabled) {
        User user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    public void deleteUser(String userId) {
        if (!userRepository.existsById(UUID.fromString(userId))) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(UUID.fromString(userId));
    }

    // ==================== DASHBOARD METHODS ====================
    
    public Map<String, Object> getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalAuditLogs = auditLogRepository.count();
        
        return Map.of(
            "totalUsers", totalUsers,
            "totalAuditLogs", totalAuditLogs,
            "activeUsers", totalUsers,
            "timestamp", LocalDateTime.now()
        );
    }

    // ==================== HELPER METHODS ====================
    
    private UserManagementDTO convertToDTO(User user) {
        return new UserManagementDTO(
            user.getId().toString(),
            user.getEmail(),
            user.getFullName(),
            user.getIndexNumber(),
            user.getRole(),
            true
        );
    }
}
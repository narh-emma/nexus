package com.nexus.adminservice.service;

import com.nexus.adminservice.client.AuthServiceClient;
import com.nexus.adminservice.dto.AdminUserView;
import com.nexus.adminservice.model.AuditLog;
import com.nexus.adminservice.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AuthServiceClient authServiceClient;
    private final AuditLogRepository auditLogRepository;

    public AdminService(AuthServiceClient authServiceClient, AuditLogRepository auditLogRepository) {
        this.authServiceClient = authServiceClient;
        this.auditLogRepository = auditLogRepository;
    }

    public List<AdminUserView> listUsers(String bearerToken) {
        return authServiceClient.fetchAllUsers(bearerToken);
    }

    public void setUserEnabled(String actorId, String targetUserId, boolean enabled, String bearerToken) {
        authServiceClient.setUserEnabled(targetUserId, enabled, bearerToken);

        String action = enabled ? "USER_ENABLED" : "USER_DISABLED";
        auditLogRepository.save(new AuditLog(actorId, targetUserId, action,
                "Admin " + actorId + " set enabled=" + enabled + " for user " + targetUserId));
    }

    public Page<AuditLog> getAuditLog(int page, int size) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }
}

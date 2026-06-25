package com.nexus.adminservice.controller;

import com.nexus.adminservice.dto.AuditLogDTO;
import com.nexus.adminservice.dto.UserManagementDTO;
import com.nexus.adminservice.model.AuditLog;
import com.nexus.adminservice.service.AdminService;
import com.nexus.authservice.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Admin management endpoints for system administration")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    @Qualifier("authJwtUtil")
    private JwtUtil jwtUtil;

    // ==================== AUDIT LOG ENDPOINTS ====================

    @GetMapping("/audit-logs")
    @Operation(summary = "Get all audit logs with pagination (admin only)")
    public ResponseEntity<?> getAllAuditLogs(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actorId) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<AuditLog> logs = adminService.getAuditLogs(action, actorId, pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", logs.getContent(),
                "pagination", Map.of(
                    "currentPage", logs.getNumber(),
                    "totalPages", logs.getTotalPages(),
                    "totalItems", logs.getTotalElements(),
                    "pageSize", logs.getSize()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // FIXED: Changed @PathVariable UUID to String
    @GetMapping("/audit-logs/{id}")
    @Operation(summary = "Get audit log by ID (admin only)")
    public ResponseEntity<?> getAuditLogById(
            @PathVariable String id,  // ← CHANGED from UUID to String
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        try {
            AuditLog log = adminService.getAuditLogById(id);  // ← Pass String directly
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", log
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/audit-logs/user/{userId}")
    @Operation(summary = "Get audit logs for a specific user (admin only)")
    public ResponseEntity<?> getUserAuditLogs(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<AuditLog> logs = adminService.getUserAuditLogs(userId, pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", logs.getContent(),
                "pagination", Map.of(
                    "currentPage", logs.getNumber(),
                    "totalPages", logs.getTotalPages(),
                    "totalItems", logs.getTotalElements(),
                    "pageSize", logs.getSize()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== USER MANAGEMENT ENDPOINTS ====================

    @GetMapping("/users")
    @Operation(summary = "Get all users with pagination (admin only)")
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<UserManagementDTO> users = adminService.getAllUsers(search, pageable);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users.getContent(),
                "pagination", Map.of(
                    "currentPage", users.getNumber(),
                    "totalPages", users.getTotalPages(),
                    "totalItems", users.getTotalElements(),
                    "pageSize", users.getSize()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user details by ID (admin only)")
    public ResponseEntity<?> getUserById(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        try {
            UserManagementDTO user = adminService.getUserById(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", user
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/role")
    @Operation(summary = "Update user role (admin only)")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String userId,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        try {
            String role = request.get("role");
            if (role == null || role.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Role is required"));
            }
            
            UserManagementDTO updatedUser = adminService.updateUserRole(userId, role);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User role updated successfully",
                "data", updatedUser
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "Enable or disable user account (admin only)")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable String userId,
            @RequestBody Map<String, Boolean> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        try {
            Boolean enabled = request.get("enabled");
            if (enabled == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Enabled status is required"));
            }
            
            UserManagementDTO updatedUser = adminService.updateUserStatus(userId, enabled);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User status updated successfully",
                "data", updatedUser
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user account (admin only)")
    public ResponseEntity<?> deleteUser(
            @PathVariable String userId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== DASHBOARD STATS ENDPOINTS ====================

    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get admin dashboard statistics (admin only)")
    public ResponseEntity<?> getDashboardStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        try {
            Map<String, Object> stats = adminService.getDashboardStats();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== SYSTEM ENDPOINTS ====================

    @GetMapping("/system/health")
    @Operation(summary = "System health check (admin only)")
    public ResponseEntity<?> systemHealth(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "Admin Service"
        ));
    }

    @GetMapping("/system/info")
    @Operation(summary = "System information (admin only)")
    public ResponseEntity<?> systemInfo(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        ResponseEntity<?> adminCheck = validateAdminAccess(authHeader);
        if (adminCheck != null) return adminCheck;
        
        Map<String, Object> systemInfo = Map.of(
            "javaVersion", System.getProperty("java.version"),
            "osName", System.getProperty("os.name"),
            "osVersion", System.getProperty("os.version"),
            "availableProcessors", Runtime.getRuntime().availableProcessors(),
            "maxMemory", Runtime.getRuntime().maxMemory(),
            "totalMemory", Runtime.getRuntime().totalMemory(),
            "freeMemory", Runtime.getRuntime().freeMemory()
        );
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", systemInfo
        ));
    }

    // ==================== HELPER METHODS ====================

    private ResponseEntity<?> validateAdminAccess(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Invalid or expired token"));
        }
        
        if (!jwtUtil.isAdmin(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "error", "Admin access required"));
        }
        
        return null;
    }

    private String extractUserIdFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.extractEmail(token);
            }
        }
        return null;
    }
}
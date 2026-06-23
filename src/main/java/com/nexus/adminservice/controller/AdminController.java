package com.nexus.adminservice.controller;

import com.nexus.adminservice.dto.AdminUserView;
import com.nexus.adminservice.dto.ApiResponse;
import com.nexus.adminservice.dto.UserStatusUpdateRequest;
import com.nexus.adminservice.model.AuditLog;
import com.nexus.adminservice.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Console endpoints. All routes sit behind the gateway at
 * /api/v1/admin and require role=ADMIN (enforced in SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ApiResponse<List<AdminUserView>> listUsers(
            @RequestHeader("Authorization") String bearerToken) {
        return ApiResponse.ok(adminService.listUsers(bearerToken));
    }

    @PatchMapping("/users/{userId}/status")
    public ApiResponse<String> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UserStatusUpdateRequest request,
            @RequestHeader("Authorization") String bearerToken,
            Authentication authentication) {

        String actorId = (String) authentication.getPrincipal();
        adminService.setUserEnabled(actorId, userId, request.getEnabled(), bearerToken);
        return ApiResponse.ok("User " + userId + " enabled=" + request.getEnabled());
    }

    @GetMapping("/audit-log")
    public ApiResponse<Page<AuditLog>> getAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(adminService.getAuditLog(page, size));
    }
}

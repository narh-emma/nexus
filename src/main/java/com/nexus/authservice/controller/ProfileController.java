package com.nexus.authservice.controller;

import com.nexus.authservice.dto.ProfileRequest;
import com.nexus.authservice.dto.ProfileResponse;
import com.nexus.authservice.service.ProfileService;
import com.nexus.authservice.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "User profile management")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    @Qualifier("authJwtUtil")
    private JwtUtil jwtUtil;

    // ===== GET PROFILE =====
    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Authentication required"));
        }

        try {
            ProfileResponse profile = profileService.getProfile(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", profile
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ===== UPDATE PROFILE =====
    @PutMapping
    @Operation(summary = "Update user profile")
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody ProfileRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Authentication required"));
        }

        try {
            ProfileResponse updatedProfile = profileService.updateProfile(email, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile updated successfully",
                "data", updatedProfile
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ===== UPLOAD PROFILE PICTURE =====
    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile picture")
    public ResponseEntity<?> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Authentication required"));
        }

        try {
            String pictureUrl = profileService.uploadProfilePicture(email, file);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile picture uploaded successfully",
                "data", Map.of("profilePictureUrl", pictureUrl)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Failed to upload file: " + e.getMessage()));
        }
    }

    // ===== DELETE PROFILE PICTURE =====
    @DeleteMapping("/picture")
    @Operation(summary = "Delete profile picture")
    public ResponseEntity<?> deleteProfilePicture(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Authentication required"));
        }

        try {
            profileService.deleteProfilePicture(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Profile picture deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ===== HELPER METHOD =====
    private String extractEmailFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.extractEmail(token);
    }
}
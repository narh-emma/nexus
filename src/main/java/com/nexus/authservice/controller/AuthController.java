package com.nexus.authservice.controller;

import com.nexus.authservice.dto.LoginRequest;
import com.nexus.authservice.dto.LoginResponse;
import com.nexus.authservice.dto.RegisterRequest;
import com.nexus.authservice.model.User;
import com.nexus.authservice.service.AuthService;
import com.nexus.authservice.service.LogoutService;
import com.nexus.authservice.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "User registration, login, and token management")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    @Qualifier("authJwtUtil")
    private JwtUtil jwtUtil;

    @Autowired
    private LogoutService logoutService;  // ← ADDED

    @PostMapping("/register")
    @Operation(summary = "Register a new user (USER or ADMIN)")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User newUser = authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getIndexNumber(),
                request.getRole()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "success", true,
                    "message", "User registered successfully",
                    "email", newUser.getEmail(),
                    "role", newUser.getRole()
                ));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Email already exists!")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "error", "Email already registered"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            User loggedUser = authService.login(request.getEmail(), request.getPassword());
            
            String token = jwtUtil.generateToken(loggedUser.getEmail(), loggedUser.getRole());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Welcome back, " + loggedUser.getFullName(),
                "email", loggedUser.getEmail(),
                "fullName", loggedUser.getFullName(),
                "role", loggedUser.getRole(),
                "token", token,
                "expiresIn", 86400000
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Auth service health check")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "Auth Service is running"));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify a Bearer JWT token")
    public ResponseEntity<?> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("valid", false, "error", "Missing or malformed Authorization header"));
        }

        String token = authHeader.substring(7);
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);
            return ResponseEntity.ok(Map.of(
                "valid", true, 
                "email", email
            ));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("valid", false, "error", "Token is invalid or expired"));
    }

    // ==================== LOGOUT ENDPOINTS ====================

    @PostMapping("/logout")
    @Operation(summary = "Logout - invalidate JWT token")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Invalid token"));
        }

        String email = jwtUtil.extractEmail(token);
        logoutService.logout(token, email);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Logged out successfully"
        ));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<?> logoutAllDevices(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Invalid token"));
        }

        String email = jwtUtil.extractEmail(token);
        logoutService.logoutAllDevices(email);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Logged out from all devices successfully"
        ));
    }
}
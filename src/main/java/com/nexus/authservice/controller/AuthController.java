package com.nexus.authservice.controller;

import com.nexus.authservice.dto.LoginResponse;
import com.nexus.authservice.model.User;
import com.nexus.authservice.service.AuthService;
import com.nexus.authservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User newUser = authService.register(
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getIndexNumber()
            );
            return ResponseEntity.ok("User registered: " + newUser.getEmail());
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Email already exists!")) {
                return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email already exists", "message", "This email is already registered. Please login or use another email."));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
    

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            User loggedUser = authService.login(
                user.getEmail(),
                user.getPasswordHash()
            );
            
            String token = jwtUtil.generateToken(loggedUser.getEmail());
            
            return ResponseEntity.ok(new LoginResponse(
                "Welcome: " + loggedUser.getFullName(),
                loggedUser.getEmail(),
                loggedUser.getFullName(),
                token,
                86400000
            ));
        } catch (RuntimeException e) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public String health() {
        return "Auth Service is running!";
    }
    
    @GetMapping("/verify")
    public String verifyToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "Missing or invalid Authorization header";
        }
        
        String token = authHeader.substring(7);
        
        if (jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);
            return "Token is valid for user: " + email;
        }
        
        return "Invalid or expired token";
    }
}
package com.nexus.authservice.service;

import com.nexus.authservice.model.PasswordResetToken;
import com.nexus.authservice.model.User;
import com.nexus.authservice.repo.PasswordResetTokenRepository;
import com.nexus.authservice.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;  // ← ADDED

    // BCrypt cost factor 12 — as required by the backend plan
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 15;

    // ===== REGISTER =====
    public User register(String email, String rawPassword, String fullName, String indexNumber, String role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists!");
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setIndexNumber(indexNumber);
        user.setPreferredLanguage("en");
        user.setSignDialect("ASL");
        user.setMedicalPreferences("{}");
        
        if (role != null && role.equalsIgnoreCase("ADMIN")) {
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    // ===== LOGIN =====
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password!");
        }

        return user;
    }

    // ===== PASSWORD RESET - Generate Token =====
    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Generate 6-digit random token
        String token = String.format("%06d", new java.util.Random().nextInt(999999));
        
        // Save token
        PasswordResetToken resetToken = new PasswordResetToken(
            token,
            email,
            LocalDateTime.now().plusMinutes(RESET_TOKEN_EXPIRY_MINUTES)
        );
        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    // ===== PASSWORD RESET - Confirm =====
    public void resetPassword(String token, String newPassword) {
        // Find token
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        // Check if token is used
        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }

        // Check if token is expired
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        // Find user
        User user = userRepository.findByEmail(resetToken.getUserEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }
}
package com.nexus.authservice.service;

import com.nexus.authservice.model.PasswordResetToken;
import com.nexus.authservice.model.User;
import com.nexus.authservice.model.VerificationToken;
import com.nexus.authservice.repo.PasswordResetTokenRepository;
import com.nexus.authservice.repo.UserRepository;
import com.nexus.authservice.repo.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private SendGridEmailService emailService;  // ← FIXED: Changed from SendGrid to SendGridEmailService

    // BCrypt cost factor 12 — as required by the backend plan
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private static final int RESET_TOKEN_EXPIRY_MINUTES = 15;
    private static final int VERIFICATION_TOKEN_EXPIRY_HOURS = 24;

    @Value("${app.email.verification.enabled:true}")
    private boolean emailVerificationEnabled;  // ← ADDED: Toggle for email verification

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
        user.setEmailVerified(false);

        if (role != null && role.equalsIgnoreCase("ADMIN")) {
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }

        User saved = userRepository.save(user);

        // Only send verification email if enabled
        if (emailVerificationEnabled) {
            issueVerificationToken(saved.getId(), VerificationToken.TokenType.REGISTRATION, null, saved.getEmail());
        }

        return saved;
    }

    // ===== LOGIN =====
    public User login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password!");
        }

        // Only check verification if enabled
        if (emailVerificationEnabled && !user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in. Check your inbox for the verification link.");
        }

        return user;
    }

    // ===== EMAIL VERIFICATION (registration) =====
    public void verifyEmail(String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid verification link"));

        if (vt.getType() != VerificationToken.TokenType.REGISTRATION) {
            throw new RuntimeException("Invalid verification link");
        }
        if (vt.isUsed()) {
            throw new RuntimeException("This verification link has already been used");
        }
        if (vt.isExpired()) {
            throw new RuntimeException("Verification link has expired. Please request a new one.");
        }

        User user = userRepository.findById(vt.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        vt.setUsed(true);
        verificationTokenRepository.save(vt);
    }

    public void resendVerificationEmail(String email) {
        if (!emailVerificationEnabled) {
            throw new RuntimeException("Email verification is disabled");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        issueVerificationToken(user.getId(), VerificationToken.TokenType.REGISTRATION, null, user.getEmail());
    }

    // ===== CHANGE PASSWORD (authenticated user) =====
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ===== CHANGE EMAIL (authenticated user, re-verification required) =====
    public void requestEmailChange(String currentEmail, String currentPassword, String newEmail) {
        User user = userRepository.findByEmail(currentEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        if (userRepository.existsByEmail(newEmail)) {
            throw new RuntimeException("That email is already in use");
        }

        user.setPendingEmail(newEmail);
        userRepository.save(user);

        if (emailVerificationEnabled) {
            issueVerificationToken(user.getId(), VerificationToken.TokenType.EMAIL_CHANGE, newEmail, newEmail);
        }
    }

    public void confirmEmailChange(String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid verification link"));

        if (vt.getType() != VerificationToken.TokenType.EMAIL_CHANGE) {
            throw new RuntimeException("Invalid verification link");
        }
        if (vt.isUsed()) {
            throw new RuntimeException("This verification link has already been used");
        }
        if (vt.isExpired()) {
            throw new RuntimeException("Verification link has expired. Please request the email change again.");
        }

        User user = userRepository.findById(vt.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (vt.getPendingEmail() == null || !vt.getPendingEmail().equals(user.getPendingEmail())) {
            throw new RuntimeException("This email change request is no longer valid");
        }

        user.setEmail(vt.getPendingEmail());
        user.setPendingEmail(null);
        userRepository.save(user);

        vt.setUsed(true);
        verificationTokenRepository.save(vt);
    }

    // ===== PASSWORD RESET - Generate Token =====
    public String generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = String.format("%06d", new java.util.Random().nextInt(999999));

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
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token has already been used");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        User user = userRepository.findByEmail(resetToken.getUserEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    // ===== HELPER: create + send a verification token =====
    private void issueVerificationToken(UUID userId, VerificationToken.TokenType type, String pendingEmail, String sendTo) {
        String token = UUID.randomUUID().toString();

        VerificationToken vt = new VerificationToken(
            token,
            userId,
            type,
            pendingEmail,
            LocalDateTime.now().plusHours(VERIFICATION_TOKEN_EXPIRY_HOURS)
        );
        verificationTokenRepository.save(vt);

        if (type == VerificationToken.TokenType.REGISTRATION) {
            emailService.sendVerificationEmail(sendTo, token);
        } else {
            emailService.sendEmailChangeVerification(sendTo, token);
        }
    }
}
package com.nexus.authservice.service;

import com.nexus.authservice.model.RefreshToken;
import com.nexus.authservice.model.User;
import com.nexus.authservice.repo.RefreshTokenRepository;
import com.nexus.authservice.repo.UserRepository;
import com.nexus.authservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("authJwtUtil")
    private JwtUtil jwtUtil;

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    /**
     * Create a new refresh token for a user
     */
    public RefreshToken createRefreshToken(String email) {
        // Revoke all existing refresh tokens for this user
        refreshTokenRepository.revokeAllUserTokens(email);

        String token = jwtUtil.generateRefreshToken(email);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);
        
        RefreshToken refreshToken = new RefreshToken(token, email, expiresAt);
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Verify refresh token and get user
     */
    public Map<String, Object> verifyRefreshToken(String token) {
        // Check if token exists in database
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Check if revoked
        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        // Check if expired
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token has expired");
        }

        // Validate JWT
        if (!jwtUtil.validateRefreshToken(token)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Get user
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return Map.of(
            "user", user,
            "refreshToken", refreshToken
        );
    }

    /**
     * Revoke a specific refresh token
     */
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revoke all refresh tokens for a user
     */
    public void revokeAllUserTokens(String email) {
        refreshTokenRepository.revokeAllUserTokens(email);
    }

    /**
     * Get number of active refresh tokens for a user
     */
    public long getActiveTokenCount(String email) {
        return refreshTokenRepository.countByUserEmailAndRevokedFalse(email);
    }

    /**
     * Clean expired refresh tokens - runs every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        System.out.println("🧹 Cleaned expired refresh tokens");
    }
}
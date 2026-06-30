package com.nexus.authservice.service;

import com.nexus.authservice.model.TokenBlacklist;
import com.nexus.authservice.repo.TokenBlacklistRepository;
import com.nexus.authservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class LogoutService {

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;

    @Autowired
    @Qualifier("authJwtUtil")
    private JwtUtil jwtUtil;

    public void logout(String token, String userEmail) {
        Date expiryDate = jwtUtil.extractExpiration(token);
        LocalDateTime expiresAt = expiryDate != null ? 
            expiryDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : 
            LocalDateTime.now().plusHours(24);

        TokenBlacklist blacklistedToken = new TokenBlacklist(token, userEmail, expiresAt);
        tokenBlacklistRepository.save(blacklistedToken);
    }

    public void logoutAllDevices(String userEmail) {
        tokenBlacklistRepository.deleteByUserEmail(userEmail);
    }

    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredTokens() {
        tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        System.out.println("🧹 Cleaned expired tokens from blacklist");
    }
}
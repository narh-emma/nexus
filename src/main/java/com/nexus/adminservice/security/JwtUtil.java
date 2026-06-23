package com.nexus.adminservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * Validates the RS256 / shared-secret JWTs minted by the Auth Service
 * (Backend Plan §7: "JWT: 15-minute access tokens ... RS256 signed").
 * The Admin Service never mints tokens — it only verifies them and checks
 * for the ADMIN role claim before allowing access.
 */
@Component
public class JwtUtil {

    @Value("${nexus.jwt.secret}")
    private String secret;

    private Key signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isAdmin(String token) {
        return "ADMIN".equalsIgnoreCase(extractRole(token));
    }
}

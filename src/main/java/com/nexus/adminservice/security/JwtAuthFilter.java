package com.nexus.adminservice.security;

import com.nexus.authservice.repo.TokenBlacklistRepository;
import com.nexus.authservice.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("authJwtUtil")
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistRepository tokenBlacklistRepository;  // ← ADDED

    // ===== THIS IS CRITICAL - SKIP PUBLIC ENDPOINTS =====
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        System.out.println("🔍 JWT Filter checking path: " + path);
        
        boolean isPublic = path.startsWith("/api/v1/auth/register") ||
                           path.startsWith("/api/v1/auth/login") ||
                           path.startsWith("/api/v1/auth/health") ||
                           path.startsWith("/api/v1/auth/verify") ||
                           path.startsWith("/api/v1/health/news") ||
                           path.startsWith("/api/v1/media/first-aid") ||
                           path.startsWith("/api/v1/media/categories") ||
                           path.startsWith("/api/v1/media/offline-bundle") ||
                           path.startsWith("/api/v1/media/search") ||
                           path.startsWith("/api/v1/media/most-viewed") ||
                           path.startsWith("/api/v1/translate/languages") ||
                           path.startsWith("/h2-console") ||
                           path.startsWith("/swagger-ui") ||
                           path.startsWith("/v3/api-docs") ||
                           path.startsWith("/api-docs") ||
                           path.startsWith("/swagger-ui.html");
        
        if (isPublic) {
            System.out.println("✅ SKIPPING JWT filter for public path: " + path);
        }
        return isPublic;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        System.out.println("🔐 JWT Filter processing: " + request.getRequestURI());
        
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("❌ No Bearer token found");
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        
        // ===== CHECK IF TOKEN IS BLACKLISTED (LOGGED OUT) =====
        if (tokenBlacklistRepository.existsByToken(jwt)) {
            System.out.println("❌ Token is blacklisted (logged out)");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"error\":\"Token has been logged out\"}");
            return;
        }

        String userEmail = jwtUtil.extractEmail(jwt);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                String role = jwtUtil.extractRole(jwt);
                System.out.println("✅ Role from token: " + role);
                
                List<SimpleGrantedAuthority> authorities;
                if ("ADMIN".equalsIgnoreCase(role)) {
                    authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else {
                    authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                }
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEmail,
                    null,
                    authorities
                );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ Authentication set");
            } else {
                System.out.println("❌ Token validation failed");
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
package com.nexus.adminservice.security;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    @Qualifier("authJwtUtil")
    private JwtUtil jwtUtil;

    // List of public endpoints that don't need JWT validation
    private static final List<String> PUBLIC_ENDPOINTS = Arrays.asList(
        "/api/v1/auth/register",
        "/api/v1/auth/login",
        "/api/v1/auth/health",
        "/api/v1/auth/verify",
        "/api/v1/health/news",
        "/api/v1/health/news/alerts",
        "/api/v1/health/news/",
        "/h2-console",
        "/swagger-ui",
        "/v3/api-docs",
        "/api-docs",
        "/swagger-ui.html"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        userEmail = jwtUtil.extractEmail(jwt);

        // ===== DEBUG LOGS =====
        System.out.println("========================================");
        System.out.println("JWT Filter - Processing request: " + request.getRequestURI());
        System.out.println("Email from token: " + userEmail);

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(jwt)) {
                // ===== EXTRACT ROLE DIRECTLY FROM TOKEN =====
                String role = jwtUtil.extractRole(jwt);
                System.out.println("Role from token: " + role);
                
                // ===== CREATE AUTHORITIES BASED ON ROLE FROM TOKEN =====
                List<SimpleGrantedAuthority> authorities;
                if ("ADMIN".equalsIgnoreCase(role)) {
                    authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    System.out.println("✅ ADMIN role detected!");
                } else {
                    authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
                    System.out.println("✅ USER role detected");
                }
                
                // ===== SET AUTHENTICATION WITH ROLE FROM TOKEN =====
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEmail,
                    null,
                    authorities
                );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                System.out.println("✅ Authentication set with authorities: " + authorities);
            } else {
                System.out.println("❌ Token validation failed!");
            }
        }
        System.out.println("========================================");
        
        filterChain.doFilter(request, response);
    }
}
package com.nexus.config;

import com.nexus.adminservice.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class AuthSecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                // ============================================
                // ===== PUBLIC ENDPOINTS - NO AUTH REQUIRED =
                // ============================================
                .requestMatchers(
                    // Auth Service
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/health",
                    "/api/v1/auth/verify",
                    
                    // News Service (GET only)
                    "/api/v1/health/news",
                    "/api/v1/health/news/alerts",
                    "/api/v1/health/news/{id}",
                    
                    // Media Service (GET only)
                    "/api/v1/media/first-aid",
                    "/api/v1/media/first-aid/{id}",
                    "/api/v1/media/categories",
                    "/api/v1/media/offline-bundle",
                    "/api/v1/media/search",
                    "/api/v1/media/most-viewed",
                    
                    // Translate Service (languages only)
                    "/api/v1/translate/languages",
                    
                    // H2 Console
                    "/h2-console/**",
                    
                    // Swagger / API Docs
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()
                
                // ============================================
                // ===== ADMIN ENDPOINTS - ADMIN ROLE REQUIRED
                // ============================================
                .requestMatchers(
                    // Admin Service
                    "/api/v1/admin/**",
                    
                    // News Service (POST, DELETE)
                    "/api/v1/health/news",      // POST - publish
                    
                    // Media Service (POST, PATCH, DELETE)
                    "/api/v1/media/first-aid",  // POST, PATCH, DELETE
                    
                    // Translate Service (POST, PUT, DELETE on dictionary)
                    "/api/v1/translate/dictionary"  // POST, PUT, DELETE
                ).hasRole("ADMIN")
                
                // ============================================
                // ===== AUTHENTICATED - VALID JWT REQUIRED
                // ============================================
                .requestMatchers(
                    // Translate Service
                    "/api/v1/translate/text-to-text",
                    "/api/v1/translate/text-to-sign",
                    "/api/v1/translate/speech-to-text",
                    "/api/v1/translate/sign-to-text",
                    "/api/v1/translate/multimodal",
                    "/api/v1/translate/dictionary",  // GET only
                    "/api/v1/translate/history",
                    
                    // Media Service (view count)
                    "/api/v1/media/first-aid/{id}/view"
                ).authenticated()
                
                // ============================================
                // ===== EVERYTHING ELSE - AUTHENTICATED
                // ============================================
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"error\":\"Authentication required\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"error\":\"Access denied\"}");
                })
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
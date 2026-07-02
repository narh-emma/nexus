package main.java.com.nexus.authservice.controller;

import main.java.com.nexus.authservice.dto.PrivacySettingsRequest;
import com.nexus.authservice.model.User;
import com.nexus.authservice.repo.UserRepository;
import com.nexus.authservice.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/privacy")
@Tag(name = "Privacy", description = "User privacy and consent management")
public class PrivacyController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("authJwtUtil")
    private JwtUtil jwtUtil;

    @GetMapping("/settings")
    @Operation(summary = "Get user privacy settings")
    public ResponseEntity<?> getPrivacySettings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Authentication required"));
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", Map.of(
                "shareMedicalData", user.isShareMedicalData(),
                "receiveNotifications", user.isReceiveNotifications(),
                "dataConsent", user.isDataConsent(),
                "consentGivenAt", user.getConsentGivenAt(),
                "consentVersion", user.getConsentVersion()
            )
        ));
    }

    @PutMapping("/settings")
    @Operation(summary = "Update privacy settings")
    public ResponseEntity<?> updatePrivacySettings(
            @RequestBody PrivacySettingsRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Authentication required"));
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setShareMedicalData(request.isShareMedicalData());
        user.setReceiveNotifications(request.isReceiveNotifications());
        user.setDataConsent(request.isDataConsent());
        user.setConsentGivenAt(LocalDateTime.now());
        
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Privacy settings updated successfully"
        ));
    }

    @PostMapping("/consent")
    @Operation(summary = "Give or withdraw data consent")
    public ResponseEntity<?> updateConsent(
            @RequestBody Map<String, Boolean> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Authentication required"));
        }

        Boolean consent = request.get("consent");
        if (consent == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", "Consent value is required"));
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDataConsent(consent);
        user.setConsentGivenAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", consent ? "Consent given" : "Consent withdrawn",
            "consentGivenAt", user.getConsentGivenAt()
        ));
    }

    @DeleteMapping("/data")
    @Operation(summary = "Request data deletion (GDPR Right to Erasure)")
    public ResponseEntity<?> requestDataDeletion(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String email = extractEmailFromToken(authHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "error", "Authentication required"));
        }

        // In production: Send email to admin, mark user for deletion
        // For now: Just return success
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Data deletion request submitted. You will be contacted within 30 days."
        ));
    }

    private String extractEmailFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        return jwtUtil.extractEmail(token);
    }
}
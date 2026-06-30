package com.nexus.translate.controller;

import com.nexus.authservice.utils.JwtUtil;
import com.nexus.translate.model.SignDictionary;
import com.nexus.translate.model.TranslationLog;
import com.nexus.translate.service.TranslateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/translate")
@Tag(name = "Translate Service", description = "Translation services for text, speech, and sign language")
public class TranslateController {

    @Autowired
    private TranslateService translateService;

    @Autowired
    @Qualifier("authJwtUtil")
    private JwtUtil jwtUtil;

    // ==================== TRANSLATION ENDPOINTS ====================

    @PostMapping("/text-to-text")
    @Operation(summary = "Translate text to text")
    public ResponseEntity<?> translateTextToText(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String text = request.get("text");
            String sourceLang = request.getOrDefault("sourceLanguage", "en");
            String targetLang = request.getOrDefault("targetLanguage", "es");

            if (text == null || text.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Text is required"));
            }

            UUID userId = extractUserId(authHeader);
            Map<String, Object> result = translateService.translateTextToText(text, sourceLang, targetLang, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/text-to-sign")
    @Operation(summary = "Convert text to sign language animation")
    public ResponseEntity<?> textToSign(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String text = request.get("text");
            String language = request.getOrDefault("language", "ASL");

            if (text == null || text.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Text is required"));
            }

            UUID userId = extractUserId(authHeader);
            Map<String, Object> result = translateService.textToSign(text, language, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/speech-to-text")
    @Operation(summary = "Convert speech audio to text")
    public ResponseEntity<?> speechToText(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String audioUrl = request.get("audioUrl");
            String language = request.getOrDefault("language", "en-US");

            if (audioUrl == null || audioUrl.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Audio URL is required"));
            }

            UUID userId = extractUserId(authHeader);
            Map<String, Object> result = translateService.speechToText(audioUrl, language, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/sign-to-text")
    @Operation(summary = "Convert sign language video to text")
    public ResponseEntity<?> signToText(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String videoUrl = request.get("videoUrl");
            String language = request.getOrDefault("language", "ASL");

            if (videoUrl == null || videoUrl.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Video URL is required"));
            }

            UUID userId = extractUserId(authHeader);
            Map<String, Object> result = translateService.signToText(videoUrl, language, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/multimodal")
    @Operation(summary = "Multi-modal translation (text, speech, sign)")
    public ResponseEntity<?> translateMultimodal(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            UUID userId = extractUserId(authHeader);
            Map<String, Object> result = translateService.processMultimodalTranslation(request, userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== DICTIONARY ENDPOINTS ====================

    @GetMapping("/dictionary")
    @Operation(summary = "Get sign language dictionary")
    public ResponseEntity<?> getDictionary(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String search) {
        try {
            List<SignDictionary> dictionary;
            if (search != null && !search.isEmpty()) {
                dictionary = translateService.searchDictionary(search);
            } else if (language != null && !language.isEmpty()) {
                dictionary = translateService.getDictionaryByLanguage(language);
            } else {
                dictionary = translateService.getDictionary();
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", dictionary,
                "count", dictionary.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/dictionary")
    @Operation(summary = "Add dictionary entry (Admin only)")
    public ResponseEntity<?> addDictionaryEntry(
            @RequestBody SignDictionary entry,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (!isAdmin(authHeader)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Admin access required"));
            }

            SignDictionary saved = translateService.addDictionaryEntry(entry);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "success", true,
                    "message", "Dictionary entry added successfully",
                    "data", saved
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping("/dictionary/{id}")
    @Operation(summary = "Update dictionary entry (Admin only)")
    public ResponseEntity<?> updateDictionaryEntry(
            @PathVariable UUID id,
            @RequestBody SignDictionary entry,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (!isAdmin(authHeader)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Admin access required"));
            }

            SignDictionary updated = translateService.updateDictionaryEntry(id, entry);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dictionary entry updated successfully",
                "data", updated
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/dictionary/{id}")
    @Operation(summary = "Delete dictionary entry (Admin only)")
    public ResponseEntity<?> deleteDictionaryEntry(
            @PathVariable UUID id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (!isAdmin(authHeader)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "error", "Admin access required"));
            }

            translateService.deleteDictionaryEntry(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Dictionary entry deleted successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== HISTORY ENDPOINTS ====================

    @GetMapping("/history")
    @Operation(summary = "Get translation history")
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false) String modality,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            UUID userId = extractUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "User not authenticated"));
            }

            List<TranslationLog> history;
            if (modality != null && !modality.isEmpty()) {
                history = translateService.getHistoryByModality(userId, modality);
            } else {
                history = translateService.getHistory(userId);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", history,
                "count", history.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/history")
    @Operation(summary = "Clear translation history")
    public ResponseEntity<?> clearHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            UUID userId = extractUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "User not authenticated"));
            }

            translateService.clearHistory(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Translation history cleared successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== SUPPORTED LANGUAGES ====================

    @GetMapping("/languages")
    @Operation(summary = "Get supported languages")
    public ResponseEntity<?> getSupportedLanguages() {
        try {
            List<Map<String, String>> languages = translateService.getSupportedLanguages();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", languages
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ==================== HELPER METHODS ====================

    private UUID extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return null;
        }
        String email = jwtUtil.extractEmail(token);
        return UUID.nameUUIDFromBytes(email.getBytes());
    }

    private boolean isAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return false;
        }
        String token = authHeader.substring(7);
        return jwtUtil.validateToken(token) && jwtUtil.isAdmin(token);
    }
}
package com.nexus.translate.controller;

import com.nexus.translate.model.SignDictionary;
import com.nexus.translate.model.TranslationLog;
import com.nexus.translate.service.TranslateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/translate")
public class TranslateController {

    private final TranslateService translateService;

    public TranslateController(TranslateService translateService) {
        this.translateService = translateService;
    }

    @PostMapping("/multimodal")
    public ResponseEntity<Map<String, Object>> translateMultimodal(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(translateService.processMultimodalTranslation(request));
    }

    @PostMapping("/text-to-sign")
    public ResponseEntity<Map<String, Object>> textToSign(@RequestBody Map<String, Object> request) {
        request.put("sourceModality", "text");
        request.put("targetModality", "sign");
        return ResponseEntity.ok(translateService.processMultimodalTranslation(request));
    }

    @PostMapping("/speech-to-sign")
    public ResponseEntity<Map<String, Object>> speechToSign(@RequestBody Map<String, Object> request) {
        request.put("sourceModality", "speech");
        request.put("targetModality", "sign");
        return ResponseEntity.ok(translateService.processMultimodalTranslation(request));
    }

    @PostMapping("/sign-to-text")
    public ResponseEntity<Map<String, Object>> signToText(@RequestBody Map<String, Object> request) {
        request.put("sourceModality", "sign");
        request.put("targetModality", "text");
        return ResponseEntity.ok(translateService.processMultimodalTranslation(request));
    }

    @PostMapping("/language")
    public ResponseEntity<Map<String, Object>> plainTranslation(@RequestBody Map<String, Object> request) {
        request.put("sourceModality", "text");
        request.put("targetModality", "text");
        return ResponseEntity.ok(translateService.processMultimodalTranslation(request));
    }

    @GetMapping("/dictionary")
    public ResponseEntity<List<SignDictionary>> getDictionary() {
        return ResponseEntity.ok(translateService.getDictionary());
    }

    @GetMapping("/history")
    public ResponseEntity<List<TranslationLog>> getHistory(@RequestAttribute("userId") UUID userId) {
        return ResponseEntity.ok(translateService.getHistory(userId));
    }
}
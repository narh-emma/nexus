package com.nexus.translate.service;

import com.nexus.translate.model.SignDictionary;
import com.nexus.translate.model.TranslationLog;
import com.nexus.translate.repository.SignDictionaryRepository;
import com.nexus.translate.repository.TranslationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TranslateService {

    @Autowired
    private TranslationLogRepository logRepository;

    @Autowired
    private SignDictionaryRepository dictionaryRepository;

    // ==================== TRANSLATION METHODS ====================

    public Map<String, Object> translateTextToText(String text, String sourceLang, String targetLang, UUID userId) {
        long startTime = System.currentTimeMillis();

        // Mock translation - replace with actual translation API
        String translatedText = mockTranslate(text, sourceLang, targetLang);

        TranslationLog log = new TranslationLog();
        log.setUserId(userId);
        log.setSourceModality("text");
        log.setTargetModality("text");
        log.setSourceLanguage(sourceLang);
        log.setTargetLanguage(targetLang);
        log.setInputText(text);
        log.setOutputPayload(translatedText);
        log.setLatencyMs((int) (System.currentTimeMillis() - startTime));
        log.setCreatedAt(LocalDateTime.now());
        logRepository.save(log);

        return Map.of(
            "success", true,
            "data", Map.of(
                "originalText", text,
                "translatedText", translatedText,
                "sourceLanguage", sourceLang,
                "targetLanguage", targetLang
            ),
            "latencyMs", log.getLatencyMs()
        );
    }

    public Map<String, Object> textToSign(String text, String language, UUID userId) {
        long startTime = System.currentTimeMillis();

        List<SignDictionary> dictionary = dictionaryRepository.findByLanguage(language);
        String signAnimation = generateSignAnimation(text, dictionary);

        TranslationLog log = new TranslationLog();
        log.setUserId(userId);
        log.setSourceModality("text");
        log.setTargetModality("sign");
        log.setSourceLanguage("en");
        log.setTargetLanguage(language);
        log.setInputText(text);
        log.setOutputPayload(signAnimation);
        log.setLatencyMs((int) (System.currentTimeMillis() - startTime));
        log.setCreatedAt(LocalDateTime.now());
        logRepository.save(log);

        return Map.of(
            "success", true,
            "data", Map.of(
                "text", text,
                "signAnimation", signAnimation,
                "language", language
            ),
            "latencyMs", log.getLatencyMs()
        );
    }

    public Map<String, Object> speechToText(String audioUrl, String language, UUID userId) {
        long startTime = System.currentTimeMillis();

        String transcribedText = mockSpeechToText(audioUrl, language);

        TranslationLog log = new TranslationLog();
        log.setUserId(userId);
        log.setSourceModality("speech");
        log.setTargetModality("text");
        log.setSourceLanguage(language);
        log.setTargetLanguage("en");
        log.setInputText(audioUrl);
        log.setOutputPayload(transcribedText);
        log.setLatencyMs((int) (System.currentTimeMillis() - startTime));
        log.setCreatedAt(LocalDateTime.now());
        logRepository.save(log);

        return Map.of(
            "success", true,
            "data", Map.of(
                "transcribedText", transcribedText,
                "language", language
            ),
            "latencyMs", log.getLatencyMs()
        );
    }

    public Map<String, Object> signToText(String videoUrl, String language, UUID userId) {
        long startTime = System.currentTimeMillis();

        String translatedText = mockSignToText(videoUrl, language);

        TranslationLog log = new TranslationLog();
        log.setUserId(userId);
        log.setSourceModality("sign");
        log.setTargetModality("text");
        log.setSourceLanguage(language);
        log.setTargetLanguage("en");
        log.setInputText(videoUrl);
        log.setOutputPayload(translatedText);
        log.setLatencyMs((int) (System.currentTimeMillis() - startTime));
        log.setCreatedAt(LocalDateTime.now());
        logRepository.save(log);

        return Map.of(
            "success", true,
            "data", Map.of(
                "text", translatedText,
                "language", language
            ),
            "latencyMs", log.getLatencyMs()
        );
    }

    public Map<String, Object> processMultimodalTranslation(Map<String, Object> request, UUID userId) {
        String sourceModality = (String) request.get("sourceModality");
        String targetModality = (String) request.get("targetModality");
        String text = (String) request.get("payload");
        String sourceLang = (String) request.get("sourceLanguage");
        String targetLang = (String) request.get("targetLanguage");

        if (sourceModality == null) sourceModality = "text";
        if (targetModality == null) targetModality = "text";

        // Route to appropriate translation method
        if (sourceModality.equals("text") && targetModality.equals("text")) {
            return translateTextToText(text, sourceLang, targetLang, userId);
        } else if (sourceModality.equals("text") && targetModality.equals("sign")) {
            return textToSign(text, targetLang != null ? targetLang : "ASL", userId);
        } else if (sourceModality.equals("speech") && targetModality.equals("text")) {
            return speechToText(text, sourceLang != null ? sourceLang : "en-US", userId);
        } else if (sourceModality.equals("sign") && targetModality.equals("text")) {
            return signToText(text, sourceLang != null ? sourceLang : "ASL", userId);
        } else {
            return Map.of(
                "success", false,
                "error", "Unsupported translation modality combination: " + sourceModality + " -> " + targetModality
            );
        }
    }

    // ==================== DICTIONARY METHODS ====================

    public List<SignDictionary> getDictionary() {
        return dictionaryRepository.findAll();
    }

    public List<SignDictionary> getDictionaryByLanguage(String language) {
        return dictionaryRepository.findByLanguage(language);
    }

    public List<SignDictionary> searchDictionary(String searchTerm) {
        return dictionaryRepository.findByWordContainingIgnoreCase(searchTerm);
    }

    public SignDictionary addDictionaryEntry(SignDictionary entry) {
        // Check if entry already exists
        if (dictionaryRepository.existsByWordAndLanguage(entry.getWord(), entry.getLanguage())) {
            throw new IllegalArgumentException("Entry already exists for word: " + entry.getWord() + " in language: " + entry.getLanguage());
        }
        entry.setCreatedAt(LocalDateTime.now());
        return dictionaryRepository.save(entry);
    }

    public SignDictionary updateDictionaryEntry(UUID id, SignDictionary entry) {
        SignDictionary existing = dictionaryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dictionary entry not found with id: " + id));

        if (entry.getWord() != null) existing.setWord(entry.getWord());
        if (entry.getSignVideoUrl() != null) existing.setSignVideoUrl(entry.getSignVideoUrl());
        if (entry.getLanguage() != null) existing.setLanguage(entry.getLanguage());
        if (entry.getDescription() != null) existing.setDescription(entry.getDescription());

        return dictionaryRepository.save(existing);
    }

    public void deleteDictionaryEntry(UUID id) {
        if (!dictionaryRepository.existsById(id)) {
            throw new RuntimeException("Dictionary entry not found with id: " + id);
        }
        dictionaryRepository.deleteById(id);
    }

    // ==================== HISTORY METHODS ====================

    public List<TranslationLog> getHistory(UUID userId) {
        return logRepository.findByUserId(userId);
    }

    public List<TranslationLog> getHistoryByModality(UUID userId, String modality) {
        return logRepository.findByUserIdAndSourceModality(userId, modality);
    }

    public void clearHistory(UUID userId) {
        logRepository.deleteByUserId(userId);
    }

    // ==================== SUPPORTED LANGUAGES ====================

    public List<Map<String, String>> getSupportedLanguages() {
        return List.of(
            Map.of("code", "en", "name", "English"),
            Map.of("code", "es", "name", "Spanish"),
            Map.of("code", "fr", "name", "French"),
            Map.of("code", "de", "name", "German"),
            Map.of("code", "it", "name", "Italian"),
            Map.of("code", "pt", "name", "Portuguese"),
            Map.of("code", "zh", "name", "Chinese"),
            Map.of("code", "ja", "name", "Japanese"),
            Map.of("code", "ko", "name", "Korean"),
            Map.of("code", "ar", "name", "Arabic"),
            Map.of("code", "ru", "name", "Russian"),
            Map.of("code", "ASL", "name", "American Sign Language"),
            Map.of("code", "BSL", "name", "British Sign Language"),
            Map.of("code", "AUSLAN", "name", "Australian Sign Language")
        );
    }

    // ==================== MOCK METHODS (Replace with actual APIs) ====================

    private String mockTranslate(String text, String sourceLang, String targetLang) {
        // TODO: Replace with Google Translate API
        return "[Translated from " + sourceLang + " to " + targetLang + "]: " + text;
    }

    private String generateSignAnimation(String text, List<SignDictionary> dictionary) {
        if (dictionary.isEmpty()) {
            return "No sign mappings found for the given language";
        }

        Map<String, String> signMap = dictionary.stream()
            .collect(Collectors.toMap(
                SignDictionary::getWord,
                SignDictionary::getSignVideoUrl,
                (existing, replacement) -> existing
            ));

        String[] words = text.split(" ");
        List<String> signs = new ArrayList<>();
        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z]", "");
            String sign = signMap.getOrDefault(cleanWord, "UNKNOWN_SIGN");
            signs.add(sign);
        }

        return String.join(" ", signs);
    }

    private String mockSpeechToText(String audioUrl, String language) {
        // TODO: Replace with Google Speech-to-Text API
        return "[Transcribed from " + language + "]: Hello, this is a test transcription.";
    }

    private String mockSignToText(String videoUrl, String language) {
        // TODO: Replace with computer vision API
        return "[Translated from sign language " + language + "]: This is a test sign language translation.";
    }
}
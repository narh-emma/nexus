package com.nexus.translate.service;

import com.nexus.translate.dto.TranslationRequest;
import com.nexus.translate.model.SignDictionary;
import com.nexus.translate.model.TranslationLog;
import com.nexus.translate.repository.SignDictionaryRepository;
import com.nexus.translate.repository.TranslationLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TranslateService {

    @Autowired
    private TranslationLogRepository logRepository;

    @Autowired
    private SignDictionaryRepository dictionaryRepository;

    @Autowired
    private HuggingFaceService huggingFaceService;

    // ==================== TEXT-TO-TEXT TRANSLATION ====================

    public Map<String, Object> translateTextToText(String text, String sourceLang, String targetLang, UUID userId) {
        long startTime = System.currentTimeMillis();

        try {
            // ===== USE HUGGING FACE FOR TRANSLATION =====
            String translatedText = huggingFaceService.translateText(text, targetLang, sourceLang);

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
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", "Translation failed: " + e.getMessage()
            );
        }
    }

    // ==================== TEXT-TO-SIGN ====================

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

    // ==================== SPEECH-TO-TEXT ====================

    public Map<String, Object> speechToText(MultipartFile audioFile, String language, UUID userId) {
        long startTime = System.currentTimeMillis();

        try {
            // ===== USE HUGGING FACE WHISPER =====
            String transcribedText = huggingFaceService.transcribeAudio(audioFile);

            TranslationLog log = new TranslationLog();
            log.setUserId(userId);
            log.setSourceModality("speech");
            log.setTargetModality("text");
            log.setSourceLanguage(language);
            log.setTargetLanguage("en");
            log.setInputText(audioFile.getOriginalFilename());
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
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", "Speech-to-text failed: " + e.getMessage()
            );
        }
    }

    // ==================== SIGN-TO-TEXT ====================

    public Map<String, Object> signToText(String videoUrl, String language, UUID userId) {
        long startTime = System.currentTimeMillis();

        // TODO: Replace with actual sign language recognition
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

    // ==================== TEXT-TO-SPEECH ====================

    public Map<String, Object> textToSpeech(String text, String voice, UUID userId) {
        long startTime = System.currentTimeMillis();

        try {
            // ===== USE HUGGING FACE TTS =====
            byte[] audioData = huggingFaceService.textToSpeech(text, voice);
            
            if (audioData.length == 0) {
                throw new RuntimeException("Failed to generate speech");
            }

            // Save audio file
            String fileName = "speech_" + UUID.randomUUID() + ".mp3";
            String audioUrl = huggingFaceService.saveAudioToFile(audioData, fileName);

            TranslationLog log = new TranslationLog();
            log.setUserId(userId);
            log.setSourceModality("text");
            log.setTargetModality("speech");
            log.setSourceLanguage("en");
            log.setTargetLanguage("en");
            log.setInputText(text);
            log.setOutputPayload(audioUrl);
            log.setLatencyMs((int) (System.currentTimeMillis() - startTime));
            log.setCreatedAt(LocalDateTime.now());
            logRepository.save(log);

            return Map.of(
                "success", true,
                "data", Map.of(
                    "text", text,
                    "audioUrl", audioUrl,
                    "voice", voice != null ? voice : "default"
                ),
                "latencyMs", log.getLatencyMs()
            );
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", "Text-to-speech failed: " + e.getMessage()
            );
        }
    }

    // ==================== PROCESS MULTIMODAL TRANSLATION ====================

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
            // For speech, we need to handle file upload differently
            return Map.of(
                "success", false,
                "error", "Please use /speech-to-text endpoint for file upload"
            );
        } else if (sourceModality.equals("sign") && targetModality.equals("text")) {
            return signToText(text, sourceLang != null ? sourceLang : "ASL", userId);
        } else {
            return Map.of(
                "success", false,
                "error", "Unsupported translation modality combination: " + sourceModality + " -> " + targetModality
            );
        }
    }

    // ==================== MULTIMODAL TRANSLATION (With DTO) ====================

    public Map<String, Object> multimodalTranslation(TranslationRequest request, UUID userId) {
        long startTime = System.currentTimeMillis();
        
        String sourceModality = request.getSourceModality();
        String targetModality = request.getTargetModality();
        String sourceLang = request.getSourceLanguage();
        String targetLang = request.getTargetLanguage();
        
        // Default values
        if (sourceModality == null || sourceModality.isEmpty()) sourceModality = "text";
        if (targetModality == null || targetModality.isEmpty()) targetModality = "text";
        if (sourceLang == null || sourceLang.isEmpty()) sourceLang = "en";
        if (targetLang == null || targetLang.isEmpty()) targetLang = "en";
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Route based on source and target modalities
            if (sourceModality.equals("text") && targetModality.equals("text")) {
                // Text → Text
                String inputText = request.getText();
                if (inputText == null) inputText = request.getPayload();
                if (inputText == null) {
                    throw new RuntimeException("Text input is required for text-to-text translation");
                }
                
                String translatedText = huggingFaceService.translateText(inputText, targetLang, sourceLang);
                
                result.put("translatedText", translatedText);
                result.put("sourceModality", "text");
                result.put("targetModality", "text");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else if (sourceModality.equals("text") && targetModality.equals("sign")) {
                // Text → Sign
                String inputText = request.getText();
                if (inputText == null) inputText = request.getPayload();
                if (inputText == null) {
                    throw new RuntimeException("Text input is required for text-to-sign translation");
                }
                
                String signDialect = request.getSignDialect();
                if (signDialect == null) signDialect = "ASL";
                
                // Get sign mappings
                List<SignDictionary> dictionary = dictionaryRepository.findByLanguage(targetLang);
                String animation = generateSignAnimation(inputText, dictionary, signDialect);
                
                result.put("translatedText", inputText);
                result.put("signAnimation", animation);
                result.put("signDialect", signDialect);
                result.put("sourceModality", "text");
                result.put("targetModality", "sign");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else if (sourceModality.equals("speech") && targetModality.equals("text")) {
                // Speech → Text
                String audioUrl = request.getAudioUrl();
                if (audioUrl == null) {
                    throw new RuntimeException("Audio URL is required for speech-to-text translation");
                }
                
                // For URL-based audio, you'd need to download first
                // This is a placeholder
                String transcribedText = "[Speech to text from URL: " + audioUrl + "]";
                
                result.put("transcribedText", transcribedText);
                result.put("sourceModality", "speech");
                result.put("targetModality", "text");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else if (sourceModality.equals("speech") && targetModality.equals("sign")) {
                // Speech → Sign
                String audioUrl = request.getAudioUrl();
                if (audioUrl == null) {
                    throw new RuntimeException("Audio URL is required for speech-to-sign translation");
                }
                
                // This is a placeholder - in reality, you'd transcribe first then convert to sign
                String transcribedText = "[Speech to sign from URL: " + audioUrl + "]";
                String signDialect = request.getSignDialect();
                if (signDialect == null) signDialect = "ASL";
                
                result.put("transcribedText", transcribedText);
                result.put("signDialect", signDialect);
                result.put("sourceModality", "speech");
                result.put("targetModality", "sign");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else if (sourceModality.equals("sign") && targetModality.equals("text")) {
                // Sign → Text
                String videoUrl = request.getVideoUrl();
                if (videoUrl == null) {
                    throw new RuntimeException("Video URL is required for sign-to-text translation");
                }
                
                String signDialect = request.getSignDialect();
                if (signDialect == null) signDialect = "ASL";
                
                String translatedText = mockSignToText(videoUrl, signDialect);
                
                result.put("translatedText", translatedText);
                result.put("signDialect", signDialect);
                result.put("sourceModality", "sign");
                result.put("targetModality", "text");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else if (sourceModality.equals("sign") && targetModality.equals("sign")) {
                // Sign → Sign
                String videoUrl = request.getVideoUrl();
                if (videoUrl == null) {
                    throw new RuntimeException("Video URL is required for sign-to-sign translation");
                }
                
                result.put("message", "Sign to sign translation uses the same sign language");
                result.put("videoUrl", videoUrl);
                result.put("sourceModality", "sign");
                result.put("targetModality", "sign");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else {
                throw new RuntimeException("Unsupported modality combination: " + sourceModality + " → " + targetModality);
            }
            
            // Calculate latency
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            result.put("latencyMs", latencyMs);
            result.put("success", true);
            
            // Log translation
            TranslationLog log = new TranslationLog();
            log.setUserId(userId);
            log.setSourceModality(sourceModality);
            log.setTargetModality(targetModality);
            log.setSourceLanguage(sourceLang);
            log.setTargetLanguage(targetLang);
            log.setInputText(request.getText() != null ? request.getText() : request.getPayload());
            log.setOutputPayload(result.toString());
            log.setLatencyMs(latencyMs);
            log.setCreatedAt(LocalDateTime.now());
            logRepository.save(log);
            
            return result;
            
        } catch (Exception e) {
            return Map.of(
                "success", false,
                "error", e.getMessage(),
                "latencyMs", (int) (System.currentTimeMillis() - startTime)
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

    // ==================== PRIVATE HELPER METHODS ====================

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

    private String generateSignAnimation(String text, List<SignDictionary> dictionary, String dialect) {
        if (dictionary.isEmpty()) {
            return "No sign mappings found for " + dialect;
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

    // ==================== MOCK METHODS (Replace with actual APIs) ====================

    private String mockSignToText(String videoUrl, String language) {
        // TODO: Replace with actual sign language recognition
        return "[Translated from sign language " + language + "]: This is a test sign language translation.";
    }
}
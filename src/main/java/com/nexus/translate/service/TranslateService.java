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
    private GroqService groqService;

    // ==================== ELEVENLABS VOICE ID CONFIGURATION ====================
    private static final Map<String, List<Map<String, String>>> LANGUAGE_VOICES = new HashMap<>();

    static {
        List<Map<String, String>> enVoices = new ArrayList<>();
        enVoices.add(Map.of("id", "21m00Tcm4TlvDq8ikWAM", "name", "Rachel (Female)"));
        enVoices.add(Map.of("id", "AZnzlk1XvdvUeBnXmlld", "name", "Domi (Female)"));
        enVoices.add(Map.of("id", "EXAVITQu4vr4xnSDxMaL", "name", "Bella (Female)"));
        enVoices.add(Map.of("id", "ErXwobaYiN019PkySvjV", "name", "Antoni (Male)"));
        enVoices.add(Map.of("id", "TxGEywOCnUeX31gOM0jB", "name", "Liam (Male)"));
        LANGUAGE_VOICES.put("en", enVoices);

        List<Map<String, String>> esVoices = new ArrayList<>();
        esVoices.add(Map.of("id", "FGY266Ew3S6t6gfI7UuG", "name", "Marcelo (Male / Spanish)"));
        esVoices.add(Map.of("id", "EXAVITQu4vr4xnSDxMaL", "name", "Bella (Multilingual)"));
        LANGUAGE_VOICES.put("es", esVoices);

        List<Map<String, String>> frVoices = new ArrayList<>();
        frVoices.add(Map.of("id", "21m00Tcm4TlvDq8ikWAM", "name", "Rachel (Multilingual)"));
        frVoices.add(Map.of("id", "ErXwobaYiN019PkySvjV", "name", "Antoni (Multilingual)"));
        LANGUAGE_VOICES.put("fr", frVoices);

        List<Map<String, String>> deVoices = new ArrayList<>();
        deVoices.add(Map.of("id", "AZnzlk1XvdvUeBnXmlld", "name", "Domi (Multilingual)"));
        deVoices.add(Map.of("id", "TxGEywOCnUeX31gOM0jB", "name", "Liam (Multilingual)"));
        LANGUAGE_VOICES.put("de", deVoices);

        List<Map<String, String>> itVoices = new ArrayList<>();
        itVoices.add(Map.of("id", "EXAVITQu4vr4xnSDxMaL", "name", "Bella (Multilingual)"));
        LANGUAGE_VOICES.put("it", itVoices);

        List<Map<String, String>> ptVoices = new ArrayList<>();
        ptVoices.add(Map.of("id", "21m00Tcm4TlvDq8ikWAM", "name", "Rachel (Multilingual)"));
        LANGUAGE_VOICES.put("pt", ptVoices);

        List<Map<String, String>> zhVoices = new ArrayList<>();
        zhVoices.add(Map.of("id", "ErXwobaYiN019PkySvjV", "name", "Antoni (Multilingual)"));
        LANGUAGE_VOICES.put("zh", zhVoices);

        List<Map<String, String>> jaVoices = new ArrayList<>();
        jaVoices.add(Map.of("id", "AZnzlk1XvdvUeBnXmlld", "name", "Domi (Multilingual)"));
        LANGUAGE_VOICES.put("ja", jaVoices);

        List<Map<String, String>> koVoices = new ArrayList<>();
        koVoices.add(Map.of("id", "EXAVITQu4vr4xnSDxMaL", "name", "Bella (Multilingual)"));
        LANGUAGE_VOICES.put("ko", koVoices);

        List<Map<String, String>> arVoices = new ArrayList<>();
        arVoices.add(Map.of("id", "TxGEywOCnUeX31gOM0jB", "name", "Liam (Multilingual)"));
        LANGUAGE_VOICES.put("ar", arVoices);

        List<Map<String, String>> ruVoices = new ArrayList<>();
        ruVoices.add(Map.of("id", "ErXwobaYiN019PkySvjV", "name", "Antoni (Multilingual)"));
        LANGUAGE_VOICES.put("ru", ruVoices);
    }

    private static final String DEFAULT_VOICE_ID = "21m00Tcm4TlvDq8ikWAM";

    private String getDefaultVoiceForLanguage(String langCode) {
        List<Map<String, String>> voices = LANGUAGE_VOICES.get(langCode != null ? langCode.toLowerCase() : "en");
        if (voices != null && !voices.isEmpty()) {
            return voices.get(0).get("id");
        }
        return DEFAULT_VOICE_ID;
    }

    // ==================== TEXT-TO-TEXT TRANSLATION ====================

    public Map<String, Object> translateTextToText(String text, String sourceLang, String targetLang, UUID userId) {
        long startTime = System.currentTimeMillis();

        try {
            String translatedText = groqService.translateText(text, targetLang);

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
            String transcribedText = groqService.transcribeAudio(audioFile);

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
        return Map.of(
            "success", false,
            "error", "Sign-to-text model integration is currently pending pipeline setup."
        );
    }

    // ==================== TEXT-TO-SPEECH ====================

    public Map<String, Object> textToSpeech(String text, String voice, String language, UUID userId) {
        long startTime = System.currentTimeMillis();

        try {
            String resolvedVoiceId = (voice != null && !voice.trim().isEmpty()) ? voice : getDefaultVoiceForLanguage(language);

            byte[] audioData = groqService.textToSpeech(text, resolvedVoiceId);
            
            if (audioData == null || audioData.length == 0) {
                throw new RuntimeException("Failed to generate speech");
            }

            String fileName = "speech_" + UUID.randomUUID() + ".mp3";
            String audioUrl = groqService.saveAudioToFile(audioData, fileName);

            TranslationLog log = new TranslationLog();
            log.setUserId(userId);
            log.setSourceModality("text");
            log.setTargetModality("speech");
            log.setSourceLanguage(language != null ? language : "en");
            log.setTargetLanguage(language != null ? language : "en");
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
                    "voice", resolvedVoiceId
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

    public Map<String, Object> textToSpeech(String text, String voice, UUID userId) {
        return textToSpeech(text, voice, "en", userId);
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

        if (sourceModality.equals("text") && targetModality.equals("text")) {
            return translateTextToText(text, sourceLang, targetLang, userId);
        } else if (sourceModality.equals("text") && targetModality.equals("sign")) {
            return textToSign(text, targetLang != null ? targetLang : "ASL", userId);
        } else if (sourceModality.equals("speech") && targetModality.equals("text")) {
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
        
        if (sourceModality == null || sourceModality.isEmpty()) sourceModality = "text";
        if (targetModality == null || targetModality.isEmpty()) targetModality = "text";
        if (sourceLang == null || sourceLang.isEmpty()) sourceLang = "en";
        if (targetLang == null || targetLang.isEmpty()) targetLang = "en";
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (sourceModality.equals("text") && targetModality.equals("text")) {
                String inputText = request.getText();
                if (inputText == null) inputText = request.getPayload();
                if (inputText == null) {
                    throw new IllegalArgumentException("Text input is required for text-to-text translation");
                }
                
                String translatedText = groqService.translateText(inputText, targetLang);
                
                result.put("translatedText", translatedText);
                result.put("sourceModality", "text");
                result.put("targetModality", "text");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else if (sourceModality.equals("text") && targetModality.equals("sign")) {
                String inputText = request.getText();
                if (inputText == null) inputText = request.getPayload();
                if (inputText == null) {
                    throw new IllegalArgumentException("Text input is required for text-to-sign translation");
                }
                
                String signDialect = request.getSignDialect();
                if (signDialect == null) signDialect = "ASL";
                
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
                String audioUrl = request.getAudioUrl();
                if (audioUrl == null) {
                    throw new IllegalArgumentException("Audio URL is required for speech-to-text translation");
                }
                
                String transcribedText = "[Speech to text processing for: " + audioUrl + "]";
                
                result.put("transcribedText", transcribedText);
                result.put("sourceModality", "speech");
                result.put("targetModality", "text");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else if (sourceModality.equals("speech") && targetModality.equals("sign")) {
                String audioUrl = request.getAudioUrl();
                if (audioUrl == null) {
                    throw new IllegalArgumentException("Audio URL is required for speech-to-sign translation");
                }
                
                String transcribedText = "[Speech processing for: " + audioUrl + "]";
                String signDialect = request.getSignDialect();
                if (signDialect == null) signDialect = "ASL";
                
                result.put("transcribedText", transcribedText);
                result.put("signDialect", signDialect);
                result.put("sourceModality", "speech");
                result.put("targetModality", "sign");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else if (sourceModality.equals("sign") && targetModality.equals("text")) {
                throw new UnsupportedOperationException("Sign-to-text pipeline is pending engine integration.");
                
            } else if (sourceModality.equals("sign") && targetModality.equals("sign")) {
                String videoUrl = request.getVideoUrl();
                if (videoUrl == null) {
                    throw new IllegalArgumentException("Video URL is required for sign-to-sign translation");
                }
                
                result.put("message", "Sign to sign translation uses the same sign language");
                result.put("videoUrl", videoUrl);
                result.put("sourceModality", "sign");
                result.put("targetModality", "sign");
                result.put("sourceLanguage", sourceLang);
                result.put("targetLanguage", targetLang);
                
            } else {
                throw new IllegalArgumentException("Unsupported modality combination: " + sourceModality + " → " + targetModality);
            }
            
            int latencyMs = (int) (System.currentTimeMillis() - startTime);
            result.put("latencyMs", latencyMs);
            result.put("success", true);
            
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

    public List<SignDictionary> getDictionary() { return dictionaryRepository.findAll(); }
    public List<SignDictionary> getDictionaryByLanguage(String language) { return dictionaryRepository.findByLanguage(language); }
    public List<SignDictionary> searchDictionary(String searchTerm) { return dictionaryRepository.findByWordContainingIgnoreCase(searchTerm); }

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

    public List<TranslationLog> getHistory(UUID userId) { return logRepository.findByUserId(userId); }
    public List<TranslationLog> getHistoryByModality(UUID userId, String modality) { return logRepository.findByUserIdAndSourceModality(userId, modality); }
    public void clearHistory(UUID userId) { logRepository.deleteByUserId(userId); }

    // ==================== SUPPORTED LANGUAGES ====================

    public List<Map<String, String>> getSupportedLanguages() {
        List<Map<String, String>> languages = new ArrayList<>();
        languages.add(Map.of("code", "en", "name", "English"));
        languages.add(Map.of("code", "es", "name", "Spanish"));
        languages.add(Map.of("code", "fr", "name", "French"));
        languages.add(Map.of("code", "de", "name", "German"));
        languages.add(Map.of("code", "it", "name", "Italian"));
        languages.add(Map.of("code", "pt", "name", "Portuguese"));
        languages.add(Map.of("code", "zh", "name", "Chinese"));
        languages.add(Map.of("code", "ja", "name", "Japanese"));
        languages.add(Map.of("code", "ko", "name", "Korean"));
        languages.add(Map.of("code", "ar", "name", "Arabic"));
        languages.add(Map.of("code", "ru", "name", "Russian"));
        languages.add(Map.of("code", "ASL", "name", "American Sign Language"));
        languages.add(Map.of("code", "BSL", "name", "British Sign Language"));
        languages.add(Map.of("code", "AUSLAN", "name", "Australian Sign Language"));
        return languages;
    }

    public Map<String, List<Map<String, String>>> getElevenLabsVoices() {
        return LANGUAGE_VOICES;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    private String generateSignAnimation(String text, List<SignDictionary> dictionary) {
        if (dictionary.isEmpty()) return "No sign mappings found for the given language";
        Map<String, String> signMap = dictionary.stream().collect(Collectors.toMap(SignDictionary::getWord, SignDictionary::getSignVideoUrl, (existing, replacement) -> existing));
        return parseTextToSigns(text, signMap);
    }

    private String generateSignAnimation(String text, List<SignDictionary> dictionary, String dialect) {
        if (dictionary.isEmpty()) return "No sign mappings found for " + dialect;
        Map<String, String> signMap = dictionary.stream().collect(Collectors.toMap(SignDictionary::getWord, SignDictionary::getSignVideoUrl, (existing, replacement) -> existing));
        return parseTextToSigns(text, signMap);
    }

    private String parseTextToSigns(String text, Map<String, String> signMap) {
        String[] words = text.split(" ");
        List<String> signs = new ArrayList<>();
        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z]", "");
            signs.add(signMap.getOrDefault(cleanWord, "UNKNOWN_SIGN"));
        }
        return String.join(" ", signs);
    }
}
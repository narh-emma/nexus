package com.nexus.translate.service;

import com.nexus.translate.model.SignDictionary;
import com.nexus.translate.model.TranslationLog;
import com.nexus.translate.repository.SignDictionaryRepository;
import com.nexus.translate.repository.TranslationLogRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TranslateService {

    private final TranslationLogRepository logRepository;
    private final SignDictionaryRepository dictionaryRepository;

    public TranslateService(TranslationLogRepository logRepository, SignDictionaryRepository dictionaryRepository) {
        this.logRepository = logRepository;
        this.dictionaryRepository = dictionaryRepository;
    }

    public Map<String, Object> processMultimodalTranslation(Map<String, Object> request) {
        long startTime = System.currentTimeMillis();
        
        // Concrete validation and parsing mapping can be added here
        String inputText = String.valueOf(request.get("payload")); 
        
        // Mocking the translation output mapping structure
        String mockPayload = "{\"translatedText\":\"Processed Input\",\"animationCues\":[]}";
        
        TranslationLog log = new TranslationLog();
        log.setSourceModality((String) request.get("sourceModality"));
        log.setTargetModality((String) request.get("targetModality"));
        log.setSourceLanguage((String) request.get("sourceLanguage"));
        log.setTargetLanguage((String) request.get("targetLanguage"));
        log.setInputText(inputText);
        log.setOutputPayload(mockPayload);
        log.setLatencyMs((int) (System.currentTimeMillis() - startTime));
        
        logRepository.save(log);
        
        return Map.of("success", true, "data", mockPayload, "latencyMs", log.getLatencyMs());
    }

    public List<SignDictionary> getDictionary() {
        return dictionaryRepository.findAll();
    }

    public List<TranslationLog> getHistory(UUID userId) {
        return logRepository.findByUserId(userId);
    }
}
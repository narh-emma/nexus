package com.nexus.translate.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TranslationResponse {
    private String translatedText;
    private List<String> glossSequence;
    private List<Map<String, Object>> animationCues;
    private String audioUrl;          // For text-to-speech
    private String signVideoUrl;      // For sign video
    private String sourceModality;
    private String targetModality;
    private String sourceLanguage;
    private String targetLanguage;
    private int latencyMs;
}
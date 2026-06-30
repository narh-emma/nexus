package com.nexus.translate.dto;

import lombok.Data;

@Data
public class TranslationRequest {
    private String sourceModality;  // text, speech, sign
    private String targetModality;  // text, sign, speech
    private String sourceLanguage;   // en, es, fr, etc.
    private String targetLanguage;   // en, es, fr, ASL, BSL, etc.
    private String signDialect;      // ASL, BSL, AUSLAN
    private String text;             // For text input
    private String audioUrl;         // For speech input
    private String videoUrl;         // For sign input
    private String payload;          // Generic payload field
}
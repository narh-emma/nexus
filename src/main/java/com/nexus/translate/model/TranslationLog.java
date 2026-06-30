package com.nexus.translate.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "translation_logs")
public class TranslationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "source_modality")
    private String sourceModality; // text, speech, sign

    @Column(name = "target_modality")
    private String targetModality; // text, sign, speech

    @Column(name = "source_language")
    private String sourceLanguage;

    @Column(name = "target_language")
    private String targetLanguage;

    @Column(name = "input_text", columnDefinition = "TEXT")
    private String inputText;

    @Column(name = "output_payload", columnDefinition = "TEXT")
    private String outputPayload;

    @Column(name = "latency_ms")
    private int latencyMs;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public TranslationLog() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getSourceModality() { return sourceModality; }
    public void setSourceModality(String sourceModality) { this.sourceModality = sourceModality; }

    public String getTargetModality() { return targetModality; }
    public void setTargetModality(String targetModality) { this.targetModality = targetModality; }

    public String getSourceLanguage() { return sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) { this.sourceLanguage = sourceLanguage; }

    public String getTargetLanguage() { return targetLanguage; }
    public void setTargetLanguage(String targetLanguage) { this.targetLanguage = targetLanguage; }

    public String getInputText() { return inputText; }
    public void setInputText(String inputText) { this.inputText = inputText; }

    public String getOutputPayload() { return outputPayload; }
    public void setOutputPayload(String outputPayload) { this.outputPayload = outputPayload; }

    public int getLatencyMs() { return latencyMs; }
    public void setLatencyMs(int latencyMs) { this.latencyMs = latencyMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
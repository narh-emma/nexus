package com.nexus.translate.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sign_dictionary")
public class SignDictionary {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String word;

    @Column(name = "sign_video_url", nullable = false)
    private String signVideoUrl;

    @Column(nullable = false)
    private String language; // ASL, BSL, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors
    public SignDictionary() {}

    public SignDictionary(String word, String signVideoUrl, String language) {
        this.word = word;
        this.signVideoUrl = signVideoUrl;
        this.language = language;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getSignVideoUrl() { return signVideoUrl; }
    public void setSignVideoUrl(String signVideoUrl) { this.signVideoUrl = signVideoUrl; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
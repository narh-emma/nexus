package com.nexus.news.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "health_ticker")
public class HealthTicker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID entryId;

    @Column(nullable = false, length = 300)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, length = 120)
    private String source;

    @Column(nullable = false, length = 500)
    private String sourceUrl;

    @Column(length = 10)
    private String language = "en";

    @Column(length = 50)
    private String category;

    private Integer priority = 0;

    @CreationTimestamp
    private LocalDateTime datePosted;

    @CreationTimestamp
    private LocalDateTime fetchedAt;

    public HealthTicker() {}

    public HealthTicker(String headline, String summary, String source,
                        String sourceUrl, String language, String category, Integer priority) {
        this.headline = headline;
        this.summary = summary;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.language = language;
        this.category = category;
        this.priority = priority;
    }

    public UUID getEntryId() { return entryId; }
    public String getHeadline() { return headline; }
    public String getSummary() { return summary; }
    public String getSource() { return source; }
    public String getSourceUrl() { return sourceUrl; }
    public String getLanguage() { return language; }
    public String getCategory() { return category; }
    public Integer getPriority() { return priority; }
    public LocalDateTime getDatePosted() { return datePosted; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }

    public void setEntryId(UUID entryId) { this.entryId = entryId; }
    public void setHeadline(String headline) { this.headline = headline; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setSource(String source) { this.source = source; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public void setLanguage(String language) { this.language = language; }
    public void setCategory(String category) { this.category = category; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public void setDatePosted(LocalDateTime datePosted) { this.datePosted = datePosted; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
}
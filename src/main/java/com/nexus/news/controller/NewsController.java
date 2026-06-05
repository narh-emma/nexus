package com.nexus.news.controller;

import com.nexus.news.model.HealthTicker;
import com.nexus.news.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "News", description = "Health news ticker — headlines, alerts, and admin controls")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @GetMapping("/news")
    @Operation(summary = "Get latest 50 headlines")
    public ResponseEntity<?> getNews(
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String category) {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", newsService.getLatestNews(lang, category)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping("/news/alerts")
    @Operation(summary = "Get alert and critical headlines only")
    public ResponseEntity<?> getAlerts() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", newsService.getAlerts()
        ));
    }

    @GetMapping("/news/{id}")
    @Operation(summary = "Get a single headline by ID")
    public ResponseEntity<?> getNewsById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", newsService.getNewsById(id)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/news")
    @Operation(summary = "Manually publish a headline (admin)")
    public ResponseEntity<?> publishHeadline(@RequestBody HealthTicker ticker) {
        try {
            HealthTicker saved = newsService.publishHeadline(ticker);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "data", saved));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/news/{id}")
    @Operation(summary = "Delete a headline (admin)")
    public ResponseEntity<?> deleteHeadline(@PathVariable UUID id) {
        try {
            newsService.deleteHeadline(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Headline deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping("/news/refresh")
    @Operation(summary = "Force a news fetch (admin)")
    public ResponseEntity<?> forceRefresh() {
        newsService.seedSampleNewsIfEmpty();
        return ResponseEntity.ok(Map.of("success", true, "message", "News refresh triggered"));
    }
}
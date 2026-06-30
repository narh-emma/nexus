package com.nexus.news.service;

import com.nexus.news.dto.NewsApiResponse;
import com.nexus.news.model.HealthTicker;
import com.nexus.news.repo.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalNewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Value("${news.api.key:}")
    private String newsApiKey;

    @Value("${news.api.base-url:https://newsapi.org/v2}")
    private String newsApiBaseUrl;

    private final WebClient webClient;

    public ExternalNewsService() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Fetch news from NewsAPI
     */
    public List<HealthTicker> fetchFromNewsAPI(String category, String language) {
        if (newsApiKey == null || newsApiKey.isEmpty()) {
            System.out.println("⚠️ NewsAPI key not configured. Skipping external fetch.");
            return new ArrayList<>();
        }

        try {
            String url = newsApiBaseUrl + "/everything?" +
                    "q=" + (category != null ? category : "health") +
                    "&language=" + (language != null ? language : "en") +
                    "&sortBy=publishedAt" +
                    "&pageSize=50" +
                    "&apiKey=" + newsApiKey;

            NewsApiResponse response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> Mono.error(new RuntimeException("API Error: " + clientResponse.statusCode())))
                    .bodyToMono(NewsApiResponse.class)
                    .block();

            if (response == null || response.getArticles() == null) {
                return new ArrayList<>();
            }

            List<HealthTicker> tickers = new ArrayList<>();
            for (NewsApiResponse.Article article : response.getArticles()) {
                if (article.getTitle() == null || article.getTitle().contains("[Removed]")) {
                    continue;
                }

                HealthTicker ticker = new HealthTicker();
                ticker.setHeadline(truncate(article.getTitle(), 300));
                ticker.setSummary(article.getDescription() != null ? truncate(article.getDescription(), 1000) : "");
                ticker.setSource(article.getSource() != null && article.getSource().getName() != null ?
                        truncate(article.getSource().getName(), 120) : "Unknown Source");
                ticker.setSourceUrl(article.getUrl() != null ? article.getUrl() : "https://example.com");
                ticker.setLanguage(language != null ? language : "en");
                ticker.setCategory(category != null ? category : "health");
                ticker.setPriority(0); // Default priority

                if (article.getPublishedAt() != null) {
                    try {
                        ZonedDateTime zonedDateTime = ZonedDateTime.parse(article.getPublishedAt());
                        ticker.setDatePosted(zonedDateTime.toLocalDateTime());
                    } catch (Exception e) {
                        ticker.setDatePosted(LocalDateTime.now());
                    }
                } else {
                    ticker.setDatePosted(LocalDateTime.now());
                }

                ticker.setFetchedAt(LocalDateTime.now());
                tickers.add(ticker);
            }

            return tickers;

        } catch (Exception e) {
            System.err.println("❌ Error fetching news from NewsAPI: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Fetch and save news from external API
     */
    public int fetchAndSaveNews(String category, String language) {
        List<HealthTicker> tickers = fetchFromNewsAPI(category, language);

        if (tickers.isEmpty()) {
            return 0;
        }

        // Check for duplicates before saving
        int savedCount = 0;
        for (HealthTicker ticker : tickers) {
            // Check if headline already exists
            if (newsRepository.findByHeadline(ticker.getHeadline()).isEmpty()) {
                newsRepository.save(ticker);
                savedCount++;
            }
        }

        System.out.println("✅ Saved " + savedCount + " new health news articles");
        return savedCount;
    }

    /**
     * Fetch health news from multiple sources
     */
    public void fetchFromMultipleSources() {
        System.out.println("📡 Fetching health news from external APIs...");

        // Fetch from NewsAPI with different categories
        String[] categories = {"health", "wellness", "nutrition", "fitness"};
        int totalSaved = 0;

        for (String category : categories) {
            int saved = fetchAndSaveNews(category, "en");
            totalSaved += saved;
        }

        System.out.println("✅ Total new articles saved: " + totalSaved);
    }

    // Helper method to truncate text
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
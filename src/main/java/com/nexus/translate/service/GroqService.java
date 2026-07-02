package com.nexus.translate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Powered by Groq:
 *  - Text-to-Text    -> chat completions (Llama 3.3)
 *  - Speech-to-Text  -> Groq-hosted Whisper (no payment method required, free tier)
 *
 * Text-to-Speech stays on EdgeTtsService (Microsoft Edge neural voices).
 */
@Service
public class GroqService {

    @Value("${groq.api.key:}")
    private String groqApiKey;

    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GroqService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    // =========================================================================
    // SPEECH-TO-TEXT (Groq-hosted Whisper)
    // =========================================================================

    public String transcribeAudio(MultipartFile audioFile) {
        return transcribeAudio(audioFile, null);
    }

    public String transcribeAudio(MultipartFile audioFile, String language) {
        try {
            if (groqApiKey == null || groqApiKey.isEmpty()) {
                return "[Mock Transcription] Groq API key not set";
            }

            String model = "whisper-large-v3-turbo"; // fastest/cheapest; use whisper-large-v3 for max accuracy

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", audioFile.getResource())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.formData()
                                    .name("file")
                                    .filename(audioFile.getOriginalFilename())
                                    .build().toString());
            builder.part("model", model);
            builder.part("response_format", "json");
            if (language != null && !language.isEmpty()) {
                builder.part("language", language);
            }

            System.out.println("📡 Transcribing with Groq Whisper (" + model + ")...");

            String response = webClient.post()
                    .uri(GROQ_BASE_URL + "/audio/transcriptions")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("Groq API Error: " + error))))
                    .bodyToMono(String.class)
                    .block();

            if (response != null && !response.isEmpty()) {
                JsonNode json = objectMapper.readTree(response);
                if (json.has("text")) {
                    return json.get("text").asText();
                }
            }
            return "";

        } catch (Exception e) {
            System.err.println("❌ Groq speech-to-text error: " + e.getMessage());
            return "[Transcription Error]";
        }
    }

    public String translateText(String text, String targetLanguage) {
        try {
            Map<String, Object> request = new HashMap<>();
            request.put("model", "llama-3.3-70b-versatile");
            request.put("temperature", 0.3);

            request.put("messages", List.of(
                    Map.of("role", "system", "content",
                            "You are an expert translator. Translate the given text directly into " + targetLanguage +
                                    ". Preserve context, format, and idioms. Do not provide explanations or chat output, ONLY return the raw translation string."),
                    Map.of("role", "user", "content", text)
            ));

            System.out.println("📡 Sending text translation task to Groq...");

            JsonNode response = webClient.post()
                    .uri(GROQ_BASE_URL + "/chat/completions")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("Groq API Error: " + error))))
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("choices") && response.path("choices").size() > 0) {
                return response.path("choices").get(0).path("message").path("content").asText().trim();
            }
            return "[Translation Failed]";

        } catch (Exception e) {
            System.err.println("❌ Groq text-to-text error: " + e.getMessage());
            return "[Translation Error]";
        }
    }
}

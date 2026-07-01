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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class HuggingFaceService {

    @Value("${huggingface.api.key:}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public HuggingFaceService() {
        this.webClient = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // ==================== TEXT-TO-TEXT TRANSLATION ====================
    // Uses LibreTranslate - Free, no API key needed

    public String translateText(String text, String targetLanguage, String sourceLanguage) {
        try {
            String url = "https://libretranslate.com/translate";

            Map<String, Object> request = new HashMap<>();
            request.put("q", text);
            request.put("source", sourceLanguage != null && !sourceLanguage.isEmpty() ? sourceLanguage : "en");
            request.put("target", targetLanguage);
            request.put("format", "text");

            System.out.println("📡 Translating with LibreTranslate...");

            String response = webClient.post()
                    .uri(url)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("API Error: " + error))))
                    .bodyToMono(String.class)
                    .block();

            if (response != null && !response.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.has("translatedText")) {
                    return jsonNode.get("translatedText").asText();
                }
            }

            return text;

        } catch (Exception e) {
            System.err.println("❌ Translation error: " + e.getMessage());
            return "[Translation Error] " + text;
        }
    }

    // ==================== SPEECH-TO-TEXT ====================
    // Uses Hugging Face Whisper

    public String transcribeAudio(MultipartFile audioFile) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                return "[Mock Transcription] Hugging Face API key not set";
            }

            String model = "openai/whisper-large-v3";

            byte[] audioBytes = audioFile.getBytes();

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", audioBytes)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.formData()
                                    .name("file")
                                    .filename(audioFile.getOriginalFilename())
                                    .build().toString());

            System.out.println("📡 Transcribing with Whisper...");

            String response = webClient.post()
                    .uri("https://api-inference.huggingface.co/models/" + model)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("API Error: " + error))))
                    .bodyToMono(String.class)
                    .block();

            if (response != null && !response.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.has("text")) {
                    return jsonNode.get("text").asText();
                }
            }

            return "";

        } catch (Exception e) {
            System.err.println("❌ Speech-to-text error: " + e.getMessage());
            return "[Transcription Error]";
        }
    }

    // ==================== TEXT-TO-SPEECH ====================
    // Uses Hugging Face FastSpeech2

    public byte[] textToSpeech(String text, String voice) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("⚠️ Hugging Face API key not set");
                return new byte[0];
            }

            String model = "facebook/fastspeech2-en-ljspeech";

            Map<String, Object> request = new HashMap<>();
            request.put("inputs", text);

            System.out.println("📡 Generating speech with FastSpeech2...");

            byte[] response = webClient.post()
                    .uri("https://api-inference.huggingface.co/models/" + model)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("API Error: " + error))))
                    .bodyToMono(byte[].class)
                    .block();

            return response != null ? response : new byte[0];

        } catch (Exception e) {
            System.err.println("❌ Text-to-speech error: " + e.getMessage());
            return new byte[0];
        }
    }

    public String saveAudioToFile(byte[] audioData, String fileName) throws IOException {
        String uploadDir = "./uploads/audio/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filePath = uploadDir + fileName;
        Path filePathObj = Paths.get(filePath);
        Files.write(filePathObj, audioData);

        return "/uploads/audio/" + fileName;
    }
}
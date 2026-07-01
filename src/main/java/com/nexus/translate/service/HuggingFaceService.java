package com.nexus.translate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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

    @Value("${huggingface.api.url:https://api-inference.huggingface.co/models}")
    private String apiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public HuggingFaceService() {
        this.webClient = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // ==================== TEXT-TO-TEXT TRANSLATION ====================

    /**
     * Translate text using Hugging Face M2M100 model
     * Supports 100+ languages
     */
    public String translateText(String text, String targetLanguage, String sourceLanguage) {
        try {
            String model = "facebook/m2m100_418M";
            
            Map<String, Object> request = new HashMap<>();
            request.put("inputs", text);
            
            Map<String, String> parameters = new HashMap<>();
            parameters.put("target_lang", targetLanguage);
            if (sourceLanguage != null && !sourceLanguage.isEmpty()) {
                parameters.put("source_lang", sourceLanguage);
            }
            request.put("parameters", parameters);

            String response = webClient.post()
                    .uri(apiUrl + "/" + model)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("API Error: " + error))))
                    .bodyToMono(String.class)
                    .block();

            if (response != null && !response.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(response);
                
                // Handle different response formats
                if (jsonNode.isArray() && jsonNode.size() > 0) {
                    JsonNode first = jsonNode.get(0);
                    if (first.has("translation_text")) {
                        return first.get("translation_text").asText();
                    }
                }
                
                if (jsonNode.has("translation_text")) {
                    return jsonNode.get("translation_text").asText();
                }
                
                if (jsonNode.has("error")) {
                    System.err.println("❌ Model error: " + jsonNode.get("error").asText());
                    return text;
                }
            }

            return text;

        } catch (Exception e) {
            System.err.println("❌ Translation error: " + e.getMessage());
            return "[Translation Error] " + text;
        }
    }

    // ==================== SPEECH-TO-TEXT ====================

    /**
     * Transcribe audio using Whisper model
     */
    public String transcribeAudio(MultipartFile audioFile) {
        try {
            String model = "openai/whisper-large-v3";
            
            byte[] audioBytes = audioFile.getBytes();

            // Build multipart request for audio
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", audioBytes)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            ContentDisposition.formData()
                                    .name("file")
                                    .filename(audioFile.getOriginalFilename())
                                    .build().toString());

            String response = webClient.post()
                    .uri(apiUrl + "/" + model)
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
                
                if (jsonNode.has("error")) {
                    System.err.println("❌ Model error: " + jsonNode.get("error").asText());
                    return "";
                }
            }

            return "";

        } catch (Exception e) {
            System.err.println("❌ Speech-to-text error: " + e.getMessage());
            return "[Transcription Error]";
        }
    }

    // ==================== TEXT-TO-SPEECH ====================

    /**
     * Convert text to speech using SpeechT5 model
     */
    public byte[] textToSpeech(String text, String voice) {
        try {
            String model = "microsoft/speecht5_tts";
            
            Map<String, Object> request = new HashMap<>();
            request.put("inputs", text);
            
            // Optional voice parameters
            if (voice != null && !voice.isEmpty()) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("voice", voice);
                request.put("parameters", parameters);
            }

            byte[] response = webClient.post()
                    .uri(apiUrl + "/" + model)
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

    /**
     * Save audio to file and return URL
     */
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
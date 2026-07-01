package com.nexus.translate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;
import java.util.Map;

/**
 * Hybrid Translation Service
 * * - Text-to-Text: Powered by Groq LLM (Llama 3.3) for blistering fast & cost-efficient translations.
 * - Speech-to-Text: Powered by ElevenLabs Scribe v2 for hyper-accurate multilingual transcriptions.
 * - Text-to-Speech: Powered by ElevenLabs (Multilingual v2/v3) for realistic, emotionally expressive voices.
 */
@Service
public class GroqService {

    // Groq configuration
    @Value("${groq.api.key:}")
    private String groqApiKey;
    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1";

    // ElevenLabs configuration
    @Value("${elevenlabs.api.key:}")
    private String elevenLabsApiKey;
    private static final String ELEVENLABS_BASE_URL = "https://api.elevenlabs.io/v1";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GroqService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    // =========================================================================
    // 1. TEXT-TO-TEXT TRANSLATION (Powered by Groq LLM)
    // =========================================================================
    
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

    // =========================================================================
    // 2. SPEECH-TO-TEXT TRANSCRIPTION (Powered by ElevenLabs Scribe v2)
    // =========================================================================
    
    public String transcribeAudio(MultipartFile audioFile) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", audioFile.getResource());
            builder.part("model_id", "scribe_v2"); // Scribe v2 handles language auto-detection & diarization natively

            System.out.println("📡 Uploading audio to ElevenLabs Scribe v2 for transcription...");

            JsonNode response = webClient.post()
                    .uri(ELEVENLABS_BASE_URL + "/speech-to-text")
                    .header("xi-api-key", elevenLabsApiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("ElevenLabs Transcription Error: " + error))))
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("text")) {
                return response.path("text").asText().trim();
            }
            return "[Transcription Failed]";

        } catch (Exception e) {
            System.err.println("❌ ElevenLabs transcription error: " + e.getMessage());
            return "[Transcription Error]";
        }
    }

    // =========================================================================
    // 3. TEXT-TO-SPEECH AUDIO SYNTHESIS (Powered by ElevenLabs Voices)
    // =========================================================================
    
    public byte[] textToSpeech(String text, String voiceId) {
        try {
            // Default to "Rachel" voice if no explicit voice ID is passed
            String selectedVoice = (voiceId != null && !voiceId.isEmpty()) ? voiceId : "21m00Tcm4TlvDq8ikWAM";

            Map<String, Object> request = new HashMap<>();
            request.put("text", text);
            // "eleven_multilingual_v2" is optimal for translation due to its multilingual text stability.
            // Alternatively, use "eleven_flash_v2_5" for real-time latency or "eleven_v3" for max theatrical/emotional expression.
            request.put("model_id", "eleven_multilingual_v2"); 

            // Standard emotional control settings
            Map<String, Object> voiceSettings = new HashMap<>();
            voiceSettings.put("stability", 0.5);
            voiceSettings.put("similarity_boost", 0.75);
            request.put("voice_settings", voiceSettings);

            System.out.println("📡 Generating natural voice audio with ElevenLabs [Voice: " + selectedVoice + "]...");

            byte[] response = webClient.post()
                    .uri(ELEVENLABS_BASE_URL + "/text-to-speech/" + selectedVoice)
                    .header("xi-api-key", elevenLabsApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("ElevenLabs TTS Error: " + error))))
                    .bodyToMono(byte[].class)
                    .block();

            return response != null ? response : new byte[0];

        } catch (Exception e) {
            System.err.println("❌ ElevenLabs text-to-speech error: " + e.getMessage());
            return new byte[0];
        }
    }

    // =========================================================================
    // UTILITY METHODS
    // =========================================================================

    public String saveAudioToFile(byte[] audioData, String fileName) throws IOException {
        String uploadDir = "./uploads/audio/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePathObj = Paths.get(uploadDir + fileName);
        Files.write(filePathObj, audioData);

        return "/uploads/audio/" + fileName;
    }
}
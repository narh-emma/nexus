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

/**
 * Speech-to-text via OpenAI's /v1/audio/transcriptions endpoint.
 *
 * Model choice (as of mid-2026):
 *  - "whisper-1"             legacy Whisper, supports srt/vtt/verbose_json output, cheapest stable option
 *  - "gpt-4o-mini-transcribe" cheaper, good accuracy, JSON/text only
 *  - "gpt-4o-transcribe"      best accuracy, JSON/text only
 *  - "gpt-4o-transcribe-diarize" adds speaker labels, JSON/text/diarized_json only
 * Max file size is 25MB; supported formats: flac, m4a, mp3, mp4, mpeg, mpga, oga, ogg, wav, webm.
 */
@Service
public class OpenAiTranscriptionService {

    @Value("${openai.api.key:}")
    private String apiKey;

    private static final String BASE_URL = "https://api.openai.com/v1";
    private static final String DEFAULT_MODEL = "gpt-4o-transcribe";

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAiTranscriptionService() {
        this.webClient = WebClient.builder().baseUrl(BASE_URL).build();
        this.objectMapper = new ObjectMapper();
    }

    public String transcribeAudio(MultipartFile audioFile) {
        return transcribeAudio(audioFile, null, null);
    }

    /**
     * @param language ISO-639-1 language hint, e.g. "en" (optional, improves accuracy)
     * @param model    override model, e.g. "whisper-1" (optional, defaults to gpt-4o-transcribe)
     */
    public String transcribeAudio(MultipartFile audioFile, String language, String model) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                return "[Mock Transcription] OpenAI API key not set";
            }

            String selectedModel = (model != null && !model.isEmpty()) ? model : DEFAULT_MODEL;

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", audioFile.getResource())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.formData()
                                    .name("file")
                                    .filename(audioFile.getOriginalFilename())
                                    .build().toString());
            builder.part("model", selectedModel);
            builder.part("response_format", "json"); // gpt-4o-transcribe models only support json/text
            if (language != null && !language.isEmpty()) {
                builder.part("language", language);
            }

            System.out.println("📡 Transcribing with OpenAI (" + selectedModel + ")...");

            String response = webClient.post()
                    .uri("/audio/transcriptions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("OpenAI API Error: " + error))))
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
            System.err.println("❌ OpenAI speech-to-text error: " + e.getMessage());
            return "[Transcription Error]";
        }
    }

    /** Foreign-language speech straight to English text (OpenAI's /translations endpoint, whisper-1 only). */
    public String translateAudioToEnglish(MultipartFile audioFile) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                return "[Mock Translation] OpenAI API key not set";
            }

            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", audioFile.getResource())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.formData()
                                    .name("file")
                                    .filename(audioFile.getOriginalFilename())
                                    .build().toString());
            builder.part("model", "whisper-1"); // translations endpoint only supports whisper-1
            builder.part("response_format", "json");

            String response = webClient.post()
                    .uri("/audio/translations")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(builder.build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class)
                                    .flatMap(error -> Mono.error(new RuntimeException("OpenAI API Error: " + error))))
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
            System.err.println("❌ OpenAI audio translation error: " + e.getMessage());
            return "[Transcription Error]";
        }
    }
}

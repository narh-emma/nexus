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

    @Value("${huggingface.api.url:https://router.huggingface.co/hf-inference/models}")
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
     * Translate text using Hugging Face mBART-50 many-to-many model
     * Supports 50 languages, confirmed available on the hf-inference provider
     */
    public String translateText(String text, String targetLanguage, String sourceLanguage) {
        try {
            String model = "facebook/mbart-large-50-many-to-many-mmt";

            Map<String, Object> request = new HashMap<>();
            request.put("inputs", text);

            Map<String, String> parameters = new HashMap<>();
            parameters.put("src_lang", toMbartLangCode(sourceLanguage != null && !sourceLanguage.isEmpty() ? sourceLanguage : "en"));
            parameters.put("tgt_lang", toMbartLangCode(targetLanguage));
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

    /**
     * Maps standard ISO 639-1 codes (en, es, fr...) to the specific
     * language tags mBART-50 expects (en_XX, es_XX, fr_XX...).
     * Falls back to English if the code isn't in the map.
     * Full list of supported codes: https://huggingface.co/facebook/mbart-large-50-many-to-many-mmt
     */
    private String toMbartLangCode(String isoCode) {
        Map<String, String> mbartCodes = new HashMap<>();
        mbartCodes.put("ar", "ar_AR");
        mbartCodes.put("cs", "cs_CZ");
        mbartCodes.put("de", "de_DE");
        mbartCodes.put("en", "en_XX");
        mbartCodes.put("es", "es_XX");
        mbartCodes.put("et", "et_EE");
        mbartCodes.put("fi", "fi_FI");
        mbartCodes.put("fr", "fr_XX");
        mbartCodes.put("gu", "gu_IN");
        mbartCodes.put("hi", "hi_IN");
        mbartCodes.put("it", "it_IT");
        mbartCodes.put("ja", "ja_XX");
        mbartCodes.put("kk", "kk_KZ");
        mbartCodes.put("ko", "ko_KR");
        mbartCodes.put("lt", "lt_LT");
        mbartCodes.put("lv", "lv_LV");
        mbartCodes.put("my", "my_MM");
        mbartCodes.put("ne", "ne_NP");
        mbartCodes.put("nl", "nl_XX");
        mbartCodes.put("ro", "ro_RO");
        mbartCodes.put("ru", "ru_RU");
        mbartCodes.put("si", "si_LK");
        mbartCodes.put("tr", "tr_TR");
        mbartCodes.put("vi", "vi_VN");
        mbartCodes.put("zh", "zh_CN");
        mbartCodes.put("af", "af_ZA");
        mbartCodes.put("az", "az_AZ");
        mbartCodes.put("bn", "bn_IN");
        mbartCodes.put("fa", "fa_IR");
        mbartCodes.put("he", "he_IL");
        mbartCodes.put("hr", "hr_HR");
        mbartCodes.put("id", "id_ID");
        mbartCodes.put("ka", "ka_GE");
        mbartCodes.put("km", "km_KH");
        mbartCodes.put("mk", "mk_MK");
        mbartCodes.put("ml", "ml_IN");
        mbartCodes.put("mn", "mn_MN");
        mbartCodes.put("mr", "mr_IN");
        mbartCodes.put("pl", "pl_PL");
        mbartCodes.put("ps", "ps_AF");
        mbartCodes.put("pt", "pt_XX");
        mbartCodes.put("sv", "sv_SE");
        mbartCodes.put("sw", "sw_KE");
        mbartCodes.put("ta", "ta_IN");
        mbartCodes.put("te", "te_IN");
        mbartCodes.put("th", "th_TH");
        mbartCodes.put("tl", "tl_XX");
        mbartCodes.put("uk", "uk_UA");
        mbartCodes.put("ur", "ur_PK");
        mbartCodes.put("xh", "xh_ZA");
        mbartCodes.put("gl", "gl_ES");
        mbartCodes.put("sl", "sl_SI");

        return mbartCodes.getOrDefault(isoCode.toLowerCase(), "en_XX");
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
     * Convert text to speech using Facebook MMS-TTS (per-language checkpoints).
     * Note: MMS-TTS has no single multilingual endpoint — each language is a
     * separate model (e.g. facebook/mms-tts-eng, facebook/mms-tts-spa), so the
     * "voice" parameter here is repurposed to carry the ISO 639-1 language code
     * (e.g. "en", "es", "fr"). Defaults to English if not provided.
     * Confirmed working on the free hf-inference provider (unlike speecht5_tts).
     * Output format is FLAC.
     */
    public byte[] textToSpeech(String text, String voice) {
        try {
            String langCode = (voice != null && !voice.isEmpty()) ? voice : "en";
            String model = "facebook/mms-tts-" + toMmsLangCode(langCode);

            Map<String, Object> request = new HashMap<>();
            request.put("inputs", text);

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
     * Maps ISO 639-1 codes (en, es, fr...) to the ISO 639-3 suffixes
     * MMS-TTS checkpoints use (eng, spa, fra...).
     * Falls back to English if the code isn't in the map.
     * Full language list: https://huggingface.co/facebook/mms-tts (Language Coverage Overview)
     */
    private String toMmsLangCode(String isoCode) {
        Map<String, String> mmsCodes = new HashMap<>();
        mmsCodes.put("en", "eng");
        mmsCodes.put("es", "spa");
        mmsCodes.put("fr", "fra");
        mmsCodes.put("de", "deu");
        mmsCodes.put("it", "ita");
        mmsCodes.put("pt", "por");
        mmsCodes.put("ru", "rus");
        mmsCodes.put("zh", "cmn");
        mmsCodes.put("ar", "ara");
        mmsCodes.put("hi", "hin");
        mmsCodes.put("ja", "jpn");
        mmsCodes.put("ko", "kor");
        mmsCodes.put("nl", "nld");
        mmsCodes.put("pl", "pol");
        mmsCodes.put("tr", "tur");
        mmsCodes.put("vi", "vie");
        mmsCodes.put("sw", "swh");
        mmsCodes.put("uk", "ukr");
        mmsCodes.put("ro", "ron");
        mmsCodes.put("el", "ell");
        mmsCodes.put("he", "heb");
        mmsCodes.put("th", "tha");
        mmsCodes.put("id", "ind");
        mmsCodes.put("bn", "ben");
        mmsCodes.put("ta", "tam");
        mmsCodes.put("te", "tel");
        mmsCodes.put("ur", "urd");
        mmsCodes.put("fa", "fas");

        return mmsCodes.getOrDefault(isoCode.toLowerCase(), "eng");
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
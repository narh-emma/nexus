package com.nexus.translate.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Text-to-speech via Microsoft Edge's "Read Aloud" voices, using the edge-tts
 * CLI as a subprocess.
 *
 * IMPORTANT: Edge TTS is NOT an official Microsoft API. It's a reverse-engineered
 * WebSocket protocol (wss://speech.platform.bing.com/.../readaloud/edge/v1) that
 * requires replicating an internal DRM token (Sec-MS-GEC) and exact Edge browser
 * headers. There's no key, no contract, no SLA -- Microsoft currently tolerates
 * community use of it, but it can change or stop working without notice.
 *
 * Rather than hand-reimplementing that undocumented handshake in Java (fragile,
 * and wrong-by-default without live testing against Microsoft's servers), this
 * service shells out to the actively-maintained reference CLI, which the
 * community keeps in sync with Microsoft's changes.
 *
 * Setup required on the host running this service:
 *   pip install edge-tts --break-system-packages   (or in a venv)
 *   which edge-tts   # confirm it's on PATH, or set edge-tts.binary-path
 *
 * List available voices locally with: edge-tts --list-voices
 */
@Service
public class EdgeTtsService {

    // e.g. "edge-tts" if on PATH, or an absolute path like "/usr/local/bin/edge-tts"
    private final String binaryPath;

    private static final String DEFAULT_VOICE = "en-US-AvaMultilingualNeural";

    public EdgeTtsService() {
        this.binaryPath = System.getProperty("edge-tts.binary-path", "edge-tts");
    }

    /**
     * Synthesize speech with an Edge neural voice.
     *
     * @param text  text to speak
     * @param voice Edge voice short name, e.g. "en-US-AvaMultilingualNeural",
     *              "es-ES-ElviraNeural", "fr-FR-DeniseNeural" (null -> default)
     * @return MP3 audio bytes, or empty array on failure
     */
    public byte[] textToSpeech(String text, String voice) {
        Path tempOut = null;
        try {
            String selectedVoice = (voice != null && !voice.isEmpty()) ? voice : DEFAULT_VOICE;

            tempOut = Files.createTempFile("edge-tts-", ".mp3");

            ProcessBuilder pb = new ProcessBuilder(
                    binaryPath,
                    "--voice", selectedVoice,
                    "--text", text,
                    "--write-media", tempOut.toString()
            );
            pb.redirectErrorStream(true);

            System.out.println("📡 Generating speech with Edge TTS [voice: " + selectedVoice + "]...");

            Process process = pb.start();

            // Drain stdout/stderr so the process doesn't block on a full pipe
            String log = new String(process.getInputStream().readAllBytes());

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("edge-tts timed out");
            }
            if (process.exitValue() != 0) {
                throw new RuntimeException("edge-tts exited with code " + process.exitValue() + ": " + log);
            }

            byte[] audio = Files.readAllBytes(tempOut);
            if (audio.length == 0) {
                throw new RuntimeException("edge-tts produced no audio: " + log);
            }
            return audio;

        } catch (Exception e) {
            System.err.println("❌ Edge TTS error: " + e.getMessage());
            return new byte[0];
        } finally {
            if (tempOut != null) {
                try { Files.deleteIfExists(tempOut); } catch (IOException ignored) { }
            }
        }
    }

    public String textToSpeechAndSave(String text, String voice) throws IOException {
        byte[] audioData = textToSpeech(text, voice);
        if (audioData.length == 0) return null;

        String fileName = "speech_" + UUID.randomUUID() + ".mp3";
        String uploadDir = "./uploads/audio/";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Files.write(Paths.get(uploadDir + fileName), audioData);
        return "/uploads/audio/" + fileName;
    }
}

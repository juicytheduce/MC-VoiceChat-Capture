import org.vosk.Model;
import org.vosk.Recognizer;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

public class SpeechRecognitionAddon {
    private Model model;
    private Recognizer recognizer;
    private final Path logFile = Path.of("voice_transcriptions.log");
    
    public void initialize() {
        try {
            // Initialize Vosk
            model = new Model("config/vosk-model");
            recognizer = new Recognizer(model, 16000);
            
            // Write log header if file doesn't exist
            if (!Files.exists(logFile)) {
                Files.writeString(logFile, "Voice Transcription Log (UUID format)\n" +
                                          "====================================\n" +
                                          "Format: [timestamp] UUID: transcription\n\n");
            }
            
            // Register voice handler
            ClientVoicechatApi.get().getClientManager()
                .onSoundPlayback((playerUuid, audioBuffer) -> {
                    String text = convertToText(audioBuffer.getData());
                    if (!text.isBlank()) {
                        logTranscription(playerUuid, text);
                    }
                });
                
        } catch (Exception e) {
            System.err.println("[Vosk] Initialization failed: " + e.getMessage());
        }
    }

    private String convertToText(byte[] pcmData) {
        try {
            if (recognizer.acceptWaveForm(pcmData, pcmData.length)) {
                return recognizer.getResult()
                    .replaceFirst(".*\"text\"\\s*:\\s*\"([^\"]+)\".*", "$1")
                    .trim();
            }
            return ""; // Skip partial results
        } catch (Exception e) {
            return "";
        }
    }

    private void logTranscription(UUID player, String text) {
        try {
            String logEntry = String.format("[%s] %s: %s\n",
                Instant.now().toString(),
                player.toString(),
                text);
                
            Files.writeString(logFile, logEntry, 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND);
                
        } catch (Exception e) {
            System.err.println("[Vosk] Logging failed: " + e.getMessage());
        }
    }
}
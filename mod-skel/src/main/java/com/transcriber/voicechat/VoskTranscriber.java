package com.transcriber.voicechat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Simple Vosk helper that loads a model and transcribes a given WAV file.
 */
public class VoskTranscriber {

    private final Model model;

    /**
     * Initialize Vosk with the given model directory (e.g. "models/vosk-model-small-en-us-0.15").
     * @param modelPath the filesystem path to the model folder
     * @throws IOException if the model cannot be loaded
     */
    public VoskTranscriber(String modelPath) throws IOException {
        // Optional: set log level before loading the model (Vosk defaults to INFO).
        // LibVosk.setLogLevel(LibVosk.LOG_INFO); // This constant was removed in newer versions

        this.model = new Model(modelPath);
    }

    /**
     * Transcribe a WAV file at wavFilePath into a single String.
     * @param wavFilePath path to a 16kHz mono 16-bit PCM WAV file
     * @return a full JSON transcription string
     * @throws IOException if file IO or audio system fails
     * @throws UnsupportedAudioFileException if the file isn't a valid WAV
     */
    public String transcribe(String wavFilePath) throws IOException, UnsupportedAudioFileException {
        try (InputStream fis = new FileInputStream(wavFilePath);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            AudioInputStream ais = AudioSystem.getAudioInputStream(bis);
            AudioFormat baseFormat = ais.getFormat();

            // Ensure we feed Vosk 16 kHz mono 16-bit PCM:
            AudioFormat voskFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                16000,
                16,
                1,
                2,
                16000,
                false
            );
            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(voskFormat, ais);

            Recognizer recognizer = new Recognizer(model, 16000.0f);
            StringBuilder fullText = new StringBuilder();
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = pcmStream.read(buffer)) >= 0) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    fullText.append(recognizer.getResult()).append("\n");
                }
            }
            fullText.append(recognizer.getFinalResult());
            recognizer.close();
            return fullText.toString();
        }
    }

    /**
     * Save the transcription text to the specified .txt file path.
     * @param transcription the text (or JSON blobs) returned by the recognizer
     * @param txtFilePath   where to save it (e.g. "recordings/voice_uuid_20250601_123456.txt")
     * @throws IOException if writing fails
     */
    public void saveTranscription(String transcription, String txtFilePath) throws IOException {
        Files.write(Paths.get(txtFilePath), transcription.getBytes());
    }
}

package com.transcriber.voicechat;

import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A minimal Vosk helper. 
 * Given a path to a WAV file, it returns a single combined transcript.
 */
public class VoskTranscriber {

    private final Model model;

    public VoskTranscriber(String modelPath) throws IOException {
        // Ensure the Vosk native library is loaded:
        LibVosk.setLogLevel(LibVosk.LOG_INFO);
        VoskTranscriber transcriber = new VoskTranscriber("models/vosk-model-small-en-us-0.22");

    }

    /**
     * Transcribe the given WAV file to a String.
     */
    public String transcribe(String wavFilePath) throws IOException, UnsupportedAudioFileException {
        // Open the WAV file as a Java audio stream:
        try (InputStream ais = new FileInputStream(wavFilePath)) {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(ais));
            AudioFormat format = audioInputStream.getFormat();

            // Vosk expects 16 kHz mono 16-bit PCM:
            AudioFormat voskFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                16000,
                16,
                1,
                2,
                16000,
                false
            );
            AudioInputStream pcmStream = AudioSystem.getAudioInputStream(voskFormat, audioInputStream);

            Recognizer recognizer = new Recognizer(model, 16000.0f);

            byte[] buffer = new byte[4096];
            int bytesRead;
            StringBuilder fullText = new StringBuilder();

            while ((bytesRead = pcmStream.read(buffer)) >= 0) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    fullText.append(recognizer.getResult()).append("\n");
                } else {
                    // Optionally: parse partial results
                    // System.out.println("Partial: " + recognizer.getPartialResult());
                }
            }

            fullText.append(recognizer.getFinalResult());
            recognizer.close();
            return fullText.toString();
        }
    }

    /**
     * Save the given transcription text to the specified path.
     */
    public void saveTranscription(String transcription, String txtFilePath) throws IOException {
        Files.write(Paths.get(txtFilePath), transcription.getBytes());
    }
}

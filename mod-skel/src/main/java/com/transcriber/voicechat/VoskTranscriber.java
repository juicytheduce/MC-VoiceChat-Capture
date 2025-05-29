package com.transcriber.voicechat;

import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.*;

public class VoskTranscriber {

    private final Model model;

    public VoskTranscriber(String modelPath) throws IOException {
        this.model = new Model(modelPath);
    }

    public String transcribe(String wavFilePath) throws IOException {
        try (InputStream ais = new FileInputStream(wavFilePath)) {
            Recognizer recognizer = new Recognizer(model, 16000.0f);
            byte[] buffer = new byte[4096];
            int nbytes;
            while ((nbytes = ais.read(buffer)) >= 0) {
                recognizer.acceptWaveForm(buffer, nbytes);
            }
            return recognizer.getFinalResult();
        }
    }

    public void saveTranscription(String text, String outPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outPath))) {
            writer.write(text);
        }
    }
}

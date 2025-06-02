package com.transcriber.voicechat;

import java.io.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

public class OpusDecoder {
    public static String convertToWav(String opusPath, String outputDir) throws IOException {
        Path opusFile = Paths.get(opusPath);
        if (!Files.exists(opusFile) || Files.size(opusFile) == 0) {
            throw new IOException("Invalid OPUS file: " + opusPath);
        }

        String fileName = opusFile.getFileName().toString().replace(".opus", ".wav");
        String wavPath = Paths.get(outputDir, fileName).toString();
        Files.createDirectories(Paths.get(outputDir));

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-y",
            "-i", opusPath,
            "-acodec", "pcm_s16le",
            "-ar", "16000",
            "-ac", "1",
            "-hide_banner",
            "-loglevel", "error",
            wavPath
        );

        try {
            Process process = pb.start();
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroy();
                throw new IOException("FFmpeg timed out");
            }
            
            if (process.exitValue() != 0) {
                throw new IOException("FFmpeg failed with code " + process.exitValue());
            }
            
            if (!Files.exists(Paths.get(wavPath))) {
                throw new IOException("No WAV file was created");
            }
            
            return wavPath;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Conversion interrupted", e);
        }
    }
}
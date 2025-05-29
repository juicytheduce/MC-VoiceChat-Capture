package com.transcriber.voicechat;

import java.io.IOException;

public class OpusDecoder {

    /**
     * Converts an Opus file to WAV using FFmpeg.
     * @param opusPath the path to the .opus file
     * @return the path to the generated .wav file
     * @throws IOException if ffmpeg fails or file access errors occur
     * @throws InterruptedException if the process is interrupted
     */
    public static String convertToWav(String opusPath) throws IOException, InterruptedException {
        String wavPath = opusPath.replace(".opus", ".wav");
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-y", "-i", opusPath, "-ar", "16000", "-ac", "1", wavPath);
        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("ffmpeg failed with exit code " + exitCode);
        }

        return wavPath;
    }
}

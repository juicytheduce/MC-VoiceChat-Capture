package com.transcriber.voicechat;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.events.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class TranscriberPlugin implements VoicechatPlugin {

    @Override
    public String getPluginId() {
        return "transcriber";
    }

    @Override
    public void initialize(VoicechatApi api) {
        System.out.println("[Transcriber] Voice chat plugin initialized");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicPacket);
    }

    private void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getSenderConnection();
        
        if (connection == null) {
            System.err.println("Warning: received a Mic packet but connection was null.");
            return;
        }

        byte[] audioData = event.getPacket().getOpusEncodedData();
        UUID playerUUID = connection.getPlayer().getUuid();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "voice_" + playerUUID + "_" + timestamp + ".opus";
        String filePath = "recordings/" + fileName;
        String tempDir = System.getProperty("java.io.tmpdir");

        try {
            Files.createDirectories(Paths.get("recordings"));
            
            // Create proper OPUS file with full headers
            createValidOpusFile(filePath, audioData);

            System.out.println("Saved voice packet from " + playerUUID + " to " + filePath);

            // Verify file
            if (!isValidOpusFile(filePath)) {
                throw new IOException("Created invalid OPUS file");
            }

            // Convert and transcribe
            String wavPath = OpusDecoder.convertToWav(filePath, tempDir);
            try {
                VoskTranscriber transcriber = new VoskTranscriber("models/vosk-model-small-en-us-0.15");
                String transcription = transcriber.transcribe(wavPath);
                String txtPath = filePath.replace(".opus", ".txt");
                transcriber.saveTranscription(transcription, txtPath);
                System.out.println("Transcription saved to " + txtPath);
            } finally {
                Files.deleteIfExists(Paths.get(wavPath));
            }
        } catch (Exception e) {
            System.err.println("Failed to process voice packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createValidOpusFile(String path, byte[] opusData) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            // Write OggS header
            byte[] header = new byte[] {
                0x4f, 0x67, 0x67, 0x53, 0x00, 0x02, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x01, (byte) opusData.length
            };
            fos.write(header);
            fos.write(opusData);
            
            // Write OggS footer
            byte[] footer = new byte[] {
                0x4f, 0x67, 0x67, 0x53, 0x00, 0x04, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x01, 0x00
            };
            fos.write(footer);
        }
    }

    private boolean isValidOpusFile(String path) {
        try {
            Process process = Runtime.getRuntime().exec(new String[] {
                "ffmpeg", "-v", "error", "-i", path, "-f", "null", "-"
            });
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
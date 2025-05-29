package com.transcriber.voicechat;

import de.maxhenkel.voicechat.api.VoicechatAPI;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.audio.MicrophonePacket;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import de.maxhenkel.voicechat.api.server.ServerVoicechatApi;
import de.maxhenkel.voicechat.api.ServerPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class TranscriberPlugin implements ServerPlugin {

    private ServerVoicechatApi api;

    @Override
    public void onServerVoicechatStarted(VoicechatServerStartedEvent event) {
        api = event.getVoicechat();
        api.registerEventListener(MicrophonePacketEvent.class, this::onMicPacket);
        System.out.println("Voicechat started and MicrophonePacketEvent registered.");
    }

    @Override
    public void onServerVoicechatStopped(VoicechatServerStoppedEvent event) {
        System.out.println("Voicechat stopped.");
    }

    private void onMicPacket(MicrophonePacketEvent event) {
        MicrophonePacket packet = event.getPacket();
        VoicechatConnection connection = event.getConnection();

        byte[] audioData = packet.getOpusEncodedData();
        UUID playerUUID = connection.getPlayer().getUuid();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "voice_" + playerUUID + "_" + timestamp + ".opus";
        String filePath = "recordings/" + fileName;

        try {
            Files.createDirectories(Paths.get("recordings"));
            try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
                fos.write(audioData);
            }
            System.out.println("Saved voice packet from " + playerUUID + " to " + filePath);

            // Decode and transcribe
            String wavPath = OpusDecoder.convertToWav(filePath);
            VoskTranscriber transcriber = new VoskTranscriber("models/vosk-model-small-en-us-0.15");
            String transcription = transcriber.transcribe(wavPath);
            String txtPath = filePath.replace(".opus", ".txt");
            transcriber.saveTranscription(transcription, txtPath);
            System.out.println("Transcription saved to " + txtPath);

        } catch (Exception e) {
            System.err.println("Failed to process voice packet: " + e.getMessage());
        }
    }

    @Override
    public String getPluginId() {
        return "transcriber_plugin";
    }
}

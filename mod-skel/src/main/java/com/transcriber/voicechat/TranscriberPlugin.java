package com.transcriber.voicechat;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServer;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.VoicechatConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class TranscriberPlugin implements VoicechatPlugin {

    private static final String PLUGIN_ID = "transcriber_plugin";

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        System.out.println("Transcriber plugin initialized.");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        VoicechatServer server = event.getVoicechat();
        server.registerEventListener(MicrophonePacketEvent.class, this::onMicPacket);
        System.out.println("Voicechat server started and MicrophonePacketEvent registered.");
    }

    private void onMicPacket(MicrophonePacketEvent event) {
        VoicechatConnection connection = event.getConnection();
        if (connection == null || connection.getPlayer() == null) {
            return;
        }

        byte[] audioData = event.getPacket().getOpusEncodedData();
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

            /*
            // Your transcription logic here.
            // WARNING: Running this on the server's network thread can cause severe lag.
            // Consider running this logic in a separate, asynchronous thread.
            String wavPath = OpusDecoder.convertToWav(filePath);
            VoskTranscriber transcriber = new VoskTranscriber("models/vosk-model-small-en-us-0.15");
            String transcription = transcriber.transcribe(wavPath);
            String txtPath = filePath.replace(".opus", ".txt");
            transcriber.saveTranscription(transcription, txtPath);
            System.out.println("Transcription saved to " + txtPath);
            */

        } catch (Exception e) {
            System.err.println("Failed to process voice packet: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
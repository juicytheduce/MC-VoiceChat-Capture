package com.transcriber.voicechat;

import de.maxhenkel.voicechat.api.Voicechat;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;

import net.fabricmc.api.ModInitializer;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class TranscriberMod implements ModInitializer {

    @Override
    public void onInitialize() {
        // Register event listeners on the Voice Chat event buses:
        Voicechat.SERVER_STARTED.register(this::onVoicechatStarted);
        Voicechat.SERVER_STOPPED.register(this::onVoicechatStopped);
        Voicechat.MICROPHONE_PACKET.register(this::onMicPacket);
    }

    private void onVoicechatStarted(VoicechatServerStartedEvent event) {
        System.out.println("[TranscriberMod] Voice Chat has started on the server.");
    }

    private void onVoicechatStopped(VoicechatServerStoppedEvent event) {
        System.out.println("[TranscriberMod] Voice Chat has stopped on the server.");
    }

    private void onMicPacket(MicrophonePacketEvent event) {
        // 1) Grab the raw packet and connection immediately:
        MicrophonePacket packet = event.getPacket();
        VoicechatConnection connection = event.getConnection();

        if (connection == null) {
            System.err.println("[TranscriberMod] Warning: received a Mic packet but connection was null.");
            return;
        }

        // 2) Extract Opus data and the player's UUID:
        byte[] opusData = packet.getOpusEncodedData();
        UUID playerUUID = connection.getPlayer().getUuid();

        // 3) Build a timestamped filename:
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String baseName = "voice_" + playerUUID + "_" + timestamp;
        String opusPath = "recordings/" + baseName + ".opus";
        String wavPath  = "recordings/" + baseName + ".wav";
        String txtPath  = "recordings/" + baseName + ".txt";

        try {
            // 4) Ensure the 'recordings/' folder exists, then write the .opus:
            Files.createDirectories(Paths.get("recordings"));
            try (FileOutputStream fos = new FileOutputStream(new File(opusPath))) {
                fos.write(opusData);
            }

            System.out.println("[TranscriberMod] Saved .opus from " + playerUUID + " → " + opusPath);

            // 5) Convert .opus → .wav (using your existing OpusDecoder helper):
            //     (Assumes you have a class called OpusDecoder with a static convertToWav method.)
            OpusDecoder.convertToWav(opusPath, wavPath);
            System.out.println("[TranscriberMod] Converted to .wav → " + wavPath);

            // 6) Run Vosk on the .wav, get back a transcription String:
            VoskTranscriber transcriber = new VoskTranscriber("models/vosk-model-small-en-us-0.15");
            String transcription = transcriber.transcribe(wavPath);

            // 7) Save the transcript to a .txt file:
            transcriber.saveTranscription(transcription, txtPath);
            System.out.println("[TranscriberMod] Transcription saved → " + txtPath);

        } catch (Exception ex) {
            System.err.println("[TranscriberMod] Error processing voice packet: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}

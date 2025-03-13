public class SpeechRecognitionAddon {
    public void initialize() {
        ClientVoicechatApi clientApi = ClientVoicechatApi.get();
        
        // Listen for audio playback events
        clientApi.getClientManager().onSoundPlayback((playerUuid, audioBuffer) -> {
            byte[] pcmData = audioBuffer.getData();
            String text = convertToText(pcmData);
            displayInChat(playerUuid, text);
        });
    }

    private String convertToText(byte[] pcmData) {
        // Implement your STT integration here
        return "Transcribed: " + new String(pcmData, StandardCharsets.UTF_8);
    }

    private void displayInChat(UUID player, String text) {
        Minecraft.getInstance().gui.getChat().addMessage(
            new TextComponent("[Voice] " + player.getName() + ": " + text)
        );
    }
}
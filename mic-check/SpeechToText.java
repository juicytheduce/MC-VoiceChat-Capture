import org.vosk.LibVosk;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;

public class SpeechToText {
    public static void main(String[] args) {
        // Initialize Vosk
        LibVosk.init();

        // Path to your Vosk model (Ensure it's downloaded)
        String modelPath = "path/to/vosk-model-small-en-us";

        try (Model model = new Model(modelPath);
             Recognizer recognizer = new Recognizer(model, 16000);
             TargetDataLine line = getMicrophone()) {

            System.out.println("Listening... Speak into the microphone.");

            byte[] buffer = new byte[4096];
            line.start();

            while (true) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    if (recognizer.acceptWaveform(buffer, bytesRead)) {
                        System.out.println("Text: " + recognizer.getResult());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to get microphone input
    private static TargetDataLine getMicrophone() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone not supported.");
        }
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        return line;
    }
}

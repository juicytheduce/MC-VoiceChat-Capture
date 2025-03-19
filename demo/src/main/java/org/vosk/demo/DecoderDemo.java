import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.LibVosk;
import javax.sound.sampled.*;
import java.io.IOException;
import java.util.logging.*;
import java.util.logging.SimpleFormatter;

public class DecoderDemo {
    private static final Logger logger = Logger.getLogger(DecoderDemo.class.getName());
    public static void main(String[] args) {
        try {
            // Setup Log File
            FileHandler fileHandler = new FileHandler("vosk.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            // Load model (update path as needed)
            Model model = new Model("model");
            
            // Create recognizer
            Recognizer recognizer = new Recognizer(model, 16000);
            
            // Set up microphone
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            
            microphone.open(format);
            microphone.start();
            
            System.out.println("Speak now...");
            
            // Process audio
            byte[] buffer = new byte[4096];
            while (true) {
                int numBytesRead = microphone.read(buffer, 0, buffer.length);
                if (recognizer.acceptWaveForm(buffer, numBytesRead)) {
                    String result = recognizer.getResult();
                    System.out.println("Result: " + result);
                    logger.info("Result: " + result); // Use the logger variable
                } else {
                    System.out.println("Partial: " + recognizer.getPartialResult());
                }
            }
            
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void textFileFormat() {
        
    }
}

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GenerateAudio {
    public static void main(String[] args) throws Exception {
        File dir = new File("app/src/main/res/raw");
        dir.mkdirs();

        // 1. Gentle Chime
        createWav(new File(dir, "tone_gentle_chime.wav"), new double[][]{
            {523.25, 0.45}, {659.25, 0.45}, {783.99, 0.45}, {1046.50, 1.0}
        });

        // 2. Morning Dawn
        createWav(new File(dir, "tone_morning_dawn.wav"), new double[][]{
            {293.66, 0.35}, {392.00, 0.35}, {440.00, 0.35}, {493.88, 0.35}, {587.33, 1.0}
        });

        // 3. Digital Beep
        createWav(new File(dir, "tone_digital_beep.wav"), new double[][]{
            {880.00, 0.15}, {0, 0.08}, {880.00, 0.15}, {0, 0.08}, {1174.66, 0.35}
        });

        // 4. Cosmic Echo
        createWav(new File(dir, "tone_cosmic_echo.wav"), new double[][]{
            {440.00, 0.5}, {554.37, 0.5}, {659.25, 0.5}, {830.61, 1.2}
        });

        // 5. Wake Bell
        createWav(new File(dir, "tone_wake_bell.wav"), new double[][]{
            {587.33, 0.6}, {739.99, 0.6}, {880.00, 1.2}
        });

        System.out.println("Audio tones generated successfully!");
    }

    private static void createWav(File out, double[][] notes) throws IOException {
        int sampleRate = 44100;
        int totalSamples = 0;
        for (double[] n : notes) {
            totalSamples += (int) (sampleRate * n[1]);
        }
        int dataSize = totalSamples * 2;

        try (FileOutputStream fos = new FileOutputStream(out)) {
            // RIFF header
            fos.write("RIFF".getBytes());
            fos.write(intToBytes(dataSize + 36));
            fos.write("WAVE".getBytes());
            fos.write("fmt ".getBytes());
            fos.write(intToBytes(16)); // Subchunk1Size
            fos.write(shortToBytes((short) 1)); // PCM
            fos.write(shortToBytes((short) 1)); // Mono
            fos.write(intToBytes(sampleRate));
            fos.write(intToBytes(sampleRate * 2));
            fos.write(shortToBytes((short) 2)); // Block align
            fos.write(shortToBytes((short) 16)); // Bits per sample
            fos.write("data".getBytes());
            fos.write(intToBytes(dataSize));

            for (double[] note : notes) {
                double freq = note[0];
                double duration = note[1];
                int count = (int) (sampleRate * duration);
                for (int i = 0; i < count; i++) {
                    double t = (double) i / sampleRate;
                    double env = 1.0;
                    double attack = 0.04;
                    double decay = 0.08;
                    if (t < attack) env = t / attack;
                    else if (t > duration - decay) env = Math.max(0, (duration - t) / decay);

                    double val = 0;
                    if (freq > 0) {
                        val = Math.sin(2 * Math.PI * freq * t) * 0.75 + Math.sin(4 * Math.PI * freq * t) * 0.2;
                    }
                    short sample = (short) (val * 20000 * env);
                    fos.write(shortToBytes(sample));
                }
            }
        }
    }

    private static byte[] intToBytes(int val) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(val).array();
    }

    private static byte[] shortToBytes(short val) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(val).array();
    }
}

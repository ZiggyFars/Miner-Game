package master.world;

import java.util.Random;

public class PerlinNoise {
    private int[] permutation;
    private static final int PERMUTATION_SIZE = 512;

    public PerlinNoise() {
        permutation = new int[PERMUTATION_SIZE];
        Random random = new Random();

        for (int i = 0; i < 256; i++) {
            permutation[i] = i;
        }

        // Shuffle
        for (int i = 0; i < 256; i++) {
            int j = random.nextInt(256);
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }

        // Duplicate the array
        for (int i = 0; i < 256; i++) {
            permutation[i + 256] = permutation[i];
        }
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y) {
        int h = hash & 3; // for 2D noise
        double u = h < 2 ? x : y;
        double v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public double noise(double x, double y) {
        int xi = (int) Math.floor(x) & 255;
        int yi = (int) Math.floor(y) & 255;

        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        double u = fade(xf);
        double v = fade(yf);

        int aa = permutation[permutation[xi] + yi];
        int ab = permutation[permutation[xi] + yi + 1];
        int ba = permutation[permutation[xi + 1] + yi];
        int bb = permutation[permutation[xi + 1] + yi + 1];

        double x1 = lerp(grad(aa, xf, yf), grad(ab, xf, yf - 1), v);
        double x2 = lerp(grad(ba, xf - 1, yf), grad(bb, xf - 1, yf - 1), v);
        return (lerp(x1, x2, u) + 1) / 2; // normalize to [0, 1]
    }

    public double noise(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double frequency = 0.1;
        double amplitude = 5;
        double maxValue = 0; // Used for normalizing

        for (int i = 0; i < octaves; i++) {
            total += this.noise(x * frequency, y * frequency) * amplitude;

            maxValue += amplitude; // Accumulate max value for normalization
            amplitude *= persistence; // Decrease amplitude
            frequency *= lacunarity; // Increase frequency
        }

        return total / maxValue; // Normalize to [0, 1]
    }
}
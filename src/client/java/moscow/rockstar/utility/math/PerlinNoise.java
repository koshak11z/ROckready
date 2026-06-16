/*
 * Decompiled with CFR 0.152.
 */
package moscow.rockstar.utility.math;

import java.util.Random;

public class PerlinNoise {
    private final int[] p = new int[512];

    public PerlinNoise() {
        this(System.currentTimeMillis());
    }

    public PerlinNoise(long seed) {
        int i;
        Random random = new Random(seed);
        int[] permutation = new int[256];
        for (i = 0; i < 256; ++i) {
            permutation[i] = i;
        }
        for (i = 0; i < 256; ++i) {
            int j = random.nextInt(256 - i) + i;
            int temp = permutation[i];
            permutation[i] = permutation[j];
            permutation[j] = temp;
        }
        for (i = 0; i < 256; ++i) {
            int n = permutation[i];
            this.p[i + 256] = n;
            this.p[i] = n;
        }
    }

    public double noise(double x) {
        return this.noise(x, 0.0, 0.0);
    }

    public double noise(double x, double y) {
        return this.noise(x, y, 0.0);
    }

    public double noise(double x, double y, double z) {
        int X = (int)Math.floor(x) & 0xFF;
        int Y = (int)Math.floor(y) & 0xFF;
        int Z = (int)Math.floor(z) & 0xFF;
        x -= Math.floor(x);
        y -= Math.floor(y);
        z -= Math.floor(z);
        double u = PerlinNoise.fade(x);
        double v = PerlinNoise.fade(y);
        double w = PerlinNoise.fade(z);
        int A = this.p[X] + Y;
        int AA = this.p[A] + Z;
        int AB = this.p[A + 1] + Z;
        int B = this.p[X + 1] + Y;
        int BA = this.p[B] + Z;
        int BB = this.p[B + 1] + Z;
        return PerlinNoise.lerp(w, PerlinNoise.lerp(v, PerlinNoise.lerp(u, PerlinNoise.grad(this.p[AA], x, y, z), PerlinNoise.grad(this.p[BA], x - 1.0, y, z)), PerlinNoise.lerp(u, PerlinNoise.grad(this.p[AB], x, y - 1.0, z), PerlinNoise.grad(this.p[BB], x - 1.0, y - 1.0, z))), PerlinNoise.lerp(v, PerlinNoise.lerp(u, PerlinNoise.grad(this.p[AA + 1], x, y, z - 1.0), PerlinNoise.grad(this.p[BA + 1], x - 1.0, y, z - 1.0)), PerlinNoise.lerp(u, PerlinNoise.grad(this.p[AB + 1], x, y - 1.0, z - 1.0), PerlinNoise.grad(this.p[BB + 1], x - 1.0, y - 1.0, z - 1.0))));
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad(int hash, double x, double y, double z) {
        double u;
        int h = hash & 0xF;
        double d = u = h < 8 ? x : y;
        double v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}


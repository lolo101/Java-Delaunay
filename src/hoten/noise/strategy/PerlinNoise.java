package hoten.noise.strategy;

import hoten.geom.Point;
import hoten.noise.Noise;
import hoten.perlin.Perlin2d;

/**
 *
 * @author lbroquet
 */
public class PerlinNoise implements Noise {

    private double[][] noise;
    private final double PERSISTENCE = 4.0 / 5.0;
    private final int OCTAVE = 16;
    private final int WIDTH = 1 << 8;
    private final int HEIGHT = 1 << 8;

    @Override
    public double elevation(Point p) {
        if (noise == null) {
            noise = new Perlin2d(PERSISTENCE, OCTAVE, (int) System.nanoTime()).createArray(WIDTH, HEIGHT);
        }
        int x = (int) ((p.x + 1.0) * (WIDTH >> 1));
        int y = (int) ((p.y + 1.0) * (HEIGHT >> 1));
        return noise[x][y];
    }
}

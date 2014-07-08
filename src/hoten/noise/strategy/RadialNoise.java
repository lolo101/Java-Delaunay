package hoten.noise.strategy;

import hoten.geom.Point;
import hoten.noise.Noise;
import java.util.Random;

/**
 *
 * @author lbroquet
 */
public class RadialNoise implements Noise {

    private static final Random r = new Random();
    private static final double ISLAND_FACTOR = 1.07;  // 1.0 means no small islands; 2.0 leads to a lot
    private final int bumps = r.nextInt(5) + 1;
    private final double startAngle = r.nextDouble() * 2 * Math.PI;
    private final double dipAngle = r.nextDouble() * 2 * Math.PI;
    private final double dipWidth = r.nextDouble() * .5 + .2;

    //only the radial implementation of amitp's map generation
    //TODO implement more island shapes
    @Override
    public double elevation(Point p) {
        double angle = Math.atan2(p.y, p.x);
        double length = 0.5 * (Math.max(Math.abs(p.x), Math.abs(p.y)) + p.length());

        double r1 = 0.5 + 0.40 * Math.sin(startAngle + bumps * angle + Math.cos((bumps + 3) * angle));
        double r2 = 0.7 - 0.20 * Math.sin(startAngle + bumps * angle - Math.sin((bumps + 2) * angle));
        if (Math.abs(angle - dipAngle) < dipWidth
                || Math.abs(angle - dipAngle + 2 * Math.PI) < dipWidth
                || Math.abs(angle - dipAngle - 2 * Math.PI) < dipWidth) {
            r1 = r2 = 0.2;
        }
        return length < r1 || (length > r1 * ISLAND_FACTOR && length < r2) ? 1.0 : 0.0;
    }
}

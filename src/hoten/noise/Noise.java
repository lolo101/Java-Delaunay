package hoten.noise;

import hoten.geom.Point;
import hoten.geom.Rectangle;

/**
 *
 * @author lbroquet
 */
@FunctionalInterface
public interface Noise {

    default Point normalize(Point p, Rectangle bounds) {
        return new Point(2 * (p.x / bounds.width - 0.5), 2 * (p.y / bounds.height - 0.5));
    }

    /**
     * Returns the elevation of the given normalized point.
     * The elevation is a double in the inclusive range 0.0..1.0.
     * @param p a normalized 2d point (x and y are in the inclusive range 0.0..1.0).
     * @return
     */
    double elevation(Point p);
}

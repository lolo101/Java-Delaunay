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

    boolean isWater(Point p);
}

package hoten.geom;

/**
 * Rectangle.java
 *
 * @author Connor
 */
public class Rectangle {
    public static final int LEFT = 4;
    public static final int BOTTOM = 2;
    public static final int RIGHT = 8;
    public static final int TOP = 1;

    /**
     *
     *
     * @param point
     * @return an int with the appropriate bits set if the Point lies on the
     * corresponding bounds lines
     */
    public int check(Point point) {
        int value = 0;
        if (point.x == left) {
            value |= LEFT;
        }
        if (point.x == right) {
            value |= RIGHT;
        }
        if (point.y == top) {
            value |= TOP;
        }
        if (point.y == bottom) {
            value |= BOTTOM;
        }
        return value;
    }

    final public double x, y, width, height, right, bottom, left, top;

    public Rectangle(double x, double y, double width, double height) {
        left = this.x = x;
        top = this.y = y;
        this.width = width;
        this.height = height;
        right = x + width;
        bottom = y + height;
    }

    public boolean liesOnAxes(Point p) {
        return GenUtils.closeEnough(p.x, x, 1) || GenUtils.closeEnough(p.y, y, 1) || GenUtils.closeEnough(p.x, right, 1) || GenUtils.closeEnough(p.y, bottom, 1);
    }

    public boolean inBounds(Point p) {
        return inBounds(p.x, p.y);
    }

    public boolean inBounds(double x0, double y0) {
        return !(x0 < x || x0 > right || y0 < y || y0 > bottom);
    }
}

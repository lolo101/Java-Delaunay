package hoten.voronoi.nodename.as3delaunay;

import hoten.geom.Point;
import java.util.List;

public final class Polygon {

    private List<Point> _vertices;

    public Polygon(List<Point> vertices) {
        _vertices = vertices;
    }

    public double area() {
        return Math.abs(signedDoubleArea() * 0.5);
    }

    public Winding winding() {
        double signedDoubleArea = signedDoubleArea();
        if (signedDoubleArea < 0) {
            return Winding.CLOCKWISE;
        }
        if (signedDoubleArea > 0) {
            return Winding.COUNTERCLOCKWISE;
        }
        return Winding.NONE;
    }

    private double signedDoubleArea() {
        int n = _vertices.size();
        double signedDoubleArea = 0;
        for (int index = 0; index < n; ++index) {
            int nextIndex = (index + 1) % n;
            Point point = _vertices.get(index);
            Point next = _vertices.get(nextIndex);
            signedDoubleArea += point.x * next.y - next.x * point.y;
        }
        return signedDoubleArea;
    }
}
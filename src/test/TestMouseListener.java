package test;

import hoten.geom.Point;
import hoten.voronoi.Center;
import hoten.voronoi.Edge;
import hoten.voronoi.VoronoiGraph;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


class TestMouseListener extends MouseAdapter {

    private static final Color TRANSPARENT_WHITE = new Color(1.0f, 1.0f, 1.0f, 0.25f);
    private final VoronoiGraph v;

    TestMouseListener(VoronoiGraph v) {
        this.v = v;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        java.awt.Point location = e.getPoint();
        Point p = new Point(location.x, location.y);
        Center center = v.getCenterOf(p);
        drawRegion(e.getComponent(), center);
    }

    private static void drawRegion(Component component, Center center) {
        Graphics graphics = component.getGraphics();
        graphics.setColor(TRANSPARENT_WHITE);
        center.borders.forEach(e -> drawTriangle(graphics, center, e));
        graphics.dispose();
    }

    private static void drawTriangle(Graphics graphics, Center c, Edge e) {
        int[] xs = {(int) c.loc.x, (int) e.v0.loc.x, (int) e.v1.loc.x};
        int[] ys = {(int) c.loc.y, (int) e.v0.loc.y, (int) e.v1.loc.y};
        graphics.fillPolygon(xs, ys, 3);
    }
}

package test;

import hoten.geom.Point;
import hoten.voronoi.nodename.as3delaunay.Voronoi;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;


class TestMouseListener extends MouseAdapter {

    private static final Color TRANSPARENT_WHITE = new Color(1.0f, 1.0f, 1.0f, 0.25f);
    private final Voronoi v;

    TestMouseListener(Voronoi v) {
        this.v = v;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        java.awt.Point location = e.getPoint();
        Point p = new Point(location.x, location.y);
        Point site = v.closestSite(p);
        List<Point> region = v.region(site);
        drawRegion(e, region);
    }

    private void drawRegion(MouseEvent e, List<Point> region) {
        Component component = (Component) e.getSource();
        Graphics graphics = component.getGraphics();
        graphics.setColor(TRANSPARENT_WHITE);
        graphics.fillPolygon(asAwtPolygon(region));
        graphics.dispose();
    }

    private Polygon asAwtPolygon(List<Point> points) {
        Polygon poly = new Polygon();
        points.forEach(p -> poly.addPoint((int) p.x, (int) p.y));
        return poly;
    }
}

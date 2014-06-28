package test;

import hoten.geom.Point;
import hoten.voronoi.Center;
import hoten.voronoi.Corner;
import hoten.voronoi.VoronoiGraph;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;


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
        Center center = v.getCenterOf((int) p.x, (int) p.y);
        List<Corner> corners = center.corners;
        drawRegion(e, corners);
    }

    private void drawRegion(MouseEvent e, List<Corner> corners) {
        Component component = (Component) e.getSource();
        Graphics graphics = component.getGraphics();
        graphics.setColor(TRANSPARENT_WHITE);
        graphics.fillPolygon(asAwtPolygon(corners));
        graphics.dispose();
    }

    private Polygon asAwtPolygon(List<Corner> corners) {
        Polygon poly = new Polygon();
        corners.forEach(c -> poly.addPoint((int) c.loc.x, (int) c.loc.y));
        return poly;
    }
}

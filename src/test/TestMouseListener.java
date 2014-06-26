package test;

import hoten.voronoi.nodename.as3delaunay.Voronoi;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;


class TestMouseListener extends MouseAdapter {

    private final Voronoi v;

    public TestMouseListener(Voronoi v) {
        this.v = v;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point location = e.getPoint();
        hoten.geom.Point p = new hoten.geom.Point(location.x, location.y);
        List<hoten.geom.Point> region = v.region(p);
        System.out.println(region);
    }
}

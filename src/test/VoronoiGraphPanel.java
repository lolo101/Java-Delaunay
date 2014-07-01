package test;

import hoten.geom.Point;
import hoten.voronoi.Center;
import hoten.voronoi.Edge;
import hoten.voronoi.VoronoiGraph;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author lbroquet
 */
public class VoronoiGraphPanel extends JPanel {

    private static final Color TRANSPARENT_WHITE = new Color(1.0f, 1.0f, 1.0f, 0.25f);
    private final VoronoiGraph graph;
    private boolean drawBiomes, drawRivers, drawSites, drawCorners, drawDelaunay, drawVoronoi;
    private Center selected;

    public VoronoiGraphPanel(VoronoiGraph graph) {
        this.graph = graph;
    }

    public void setDrawBiomes(boolean drawBiomes) {
        this.drawBiomes = drawBiomes;
    }

    public void setDrawRivers(boolean drawRivers) {
        this.drawRivers = drawRivers;
    }

    public void setDrawSites(boolean drawSites) {
        this.drawSites = drawSites;
    }

    public void setDrawCorners(boolean drawCorners) {
        this.drawCorners = drawCorners;
    }

    public void setDrawDelaunay(boolean drawDelaunay) {
        this.drawDelaunay = drawDelaunay;
    }

    public void setDrawVoronoi(boolean drawVoronoi) {
        this.drawVoronoi = drawVoronoi;
    }

    public void selectRegionAt(java.awt.Point point) {
        Point p = new Point(point.x, point.y);
        Center target = graph.getCenterOf(p);
        // select/unselect
        selected = target == selected ? null : target;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        graph.paint(g2d,
                drawBiomes,
                drawRivers,
                drawSites,
                drawCorners,
                drawDelaunay,
                drawVoronoi);
        if (selected != null) {
            drawRegion(g2d, selected);
        }
    }

    private static void drawRegion(Graphics graphics, Center center) {
        graphics.setColor(TRANSPARENT_WHITE);
        center.borders.stream().filter(e -> e.v0 != null && e.v1 != null)
                .forEach(e -> drawTriangle(graphics, center, e));
    }

    private static void drawTriangle(Graphics graphics, Center c, Edge e) {
        int[] xs = {(int) c.loc.x, (int) e.v0.loc.x, (int) e.v1.loc.x};
        int[] ys = {(int) c.loc.y, (int) e.v0.loc.y, (int) e.v1.loc.y};
        graphics.fillPolygon(xs, ys, 3);
    }

}

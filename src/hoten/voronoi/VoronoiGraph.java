package hoten.voronoi;

import hoten.geom.Point;
import hoten.geom.Rectangle;
import hoten.voronoi.nodename.as3delaunay.LineSegment;
import hoten.voronoi.nodename.as3delaunay.Voronoi;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import static java.util.Map.Entry.comparingByKey;
import java.util.Random;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.minBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * VoronoiGraph.java
 *
 * @author Connor
 */
public abstract class VoronoiGraph {

    final public List<Edge> edges = new ArrayList<>();
    final public List<Corner> corners = new ArrayList<>();
    final public List<Center> centers = new ArrayList<>();
    final public Rectangle bounds;
    final private Random r;
    protected Color OCEAN, RIVER, LAKE, BEACH;

    public VoronoiGraph(Voronoi v, int numLloydRelaxations, Random r) {
        this.r = r;
        bumps = r.nextInt(5) + 1;
        startAngle = r.nextDouble() * 2 * Math.PI;
        dipAngle = r.nextDouble() * 2 * Math.PI;
        dipWidth = r.nextDouble() * .5 + .2;
        bounds = v.get_plotBounds();
        for (int i = 0; i < numLloydRelaxations; i++) {
            List<Point> points = v.siteCoords();
            for (Point p : points) {
                List<Point> region = v.region(p);
                p.x = region.stream().collect(averagingDouble(c -> c.x));
                p.y = region.stream().collect(averagingDouble(c -> c.y));
            }
            v = new Voronoi(points, v.get_plotBounds());
        }
        buildGraph(v);
        improveCorners();

        assignCornerElevations();
        assignOceanCoastAndLand();
        redistributeElevations(landCorners());
        assignPolygonElevations();

        calculateDownslopes();
        //calculateWatersheds();
        createRivers();
        assignCornerMoisture();
        redistributeMoisture(landCorners());
        assignPolygonMoisture();
        assignBiomes();
    }

    abstract protected Enum getBiome(Center p);

    abstract protected Color getColor(Enum biome);

    public Center getCenterOf(Point p) {
        return centers.stream().collect(toMap(c -> Point.distance(c.loc, p), identity()))
                .entrySet().stream().sorted(comparingByKey()).findFirst().get().getValue();
    }

    private void improveCorners() {
        for (Corner c : corners) {
            if (c.border) {
                double x = c.touches.stream().collect(averagingDouble(t -> t.loc.x));
                double y = c.touches.stream().collect(averagingDouble(t -> t.loc.y));
                c.loc = new Point(x, y);
            }
        }
        edges.stream().filter(e -> e.v0 != null && e.v1 != null).forEach(e -> e.setVornoi(e.v0, e.v1));
    }

    private Edge edgeWithCenters(Center c1, Center c2) {
        return c1.borders.stream().filter(e -> e.d0 == c2 || e.d1 == c2).findFirst().orElse(null);
    }

    private void drawTriangle(Graphics2D g, Corner c1, Corner c2, Center center) {
        int[] x = new int[3];
        int[] y = new int[3];
        x[0] = (int) center.loc.x;
        y[0] = (int) center.loc.y;
        x[1] = (int) c1.loc.x;
        y[1] = (int) c1.loc.y;
        x[2] = (int) c2.loc.x;
        y[2] = (int) c2.loc.y;
        g.fillPolygon(x, y, 3);
    }

    private boolean closeEnough(double d1, double d2, double diff) {
        return Math.abs(d1 - d2) <= diff;
    }

    public void paint(Graphics2D g) {
        paint(g, true, true, false, false, false, true);
    }

    //also records the area of each voronoi cell
    public void paint(Graphics2D g, boolean drawBiomes, boolean drawRivers, boolean drawSites, boolean drawCorners, boolean drawDelaunay, boolean drawVoronoi) {
        //draw via triangles
        for (Center c : centers) {
            g.setColor(drawBiomes ? getColor(c.biome) : c.color);

            //only used if Center c is on the edge of the graph. allows for completely filling in the outer polygons
            Corner edgeCorner1 = null;
            Corner edgeCorner2 = null;
            c.area = 0;
            for (Center n : c.neighbors) {
                Edge e = edgeWithCenters(c, n);

                if (e.v0 == null) {
                    //outermost voronoi edges aren't stored in the graph
                    continue;
                }

                //find a corner on the exterior of the graph
                //if this Edge e has one, then it must have two,
                //finding these two corners will give us the missing
                //triangle to render. this special triangle is handled
                //outside this for loop
                Corner cornerWithOneAdjacent = e.v0.border ? e.v0 : e.v1;
                if (cornerWithOneAdjacent.border) {
                    if (edgeCorner1 == null) {
                        edgeCorner1 = cornerWithOneAdjacent;
                    } else {
                        edgeCorner2 = cornerWithOneAdjacent;
                    }
                }

                drawTriangle(g, e.v0, e.v1, c);
                c.area += Math.abs(c.loc.x * (e.v0.loc.y - e.v1.loc.y)
                        + e.v0.loc.x * (e.v1.loc.y - c.loc.y)
                        + e.v1.loc.x * (c.loc.y - e.v0.loc.y)) / 2;
            }

            //handle the missing triangle
            if (edgeCorner2 != null) {
                //if these two outer corners are NOT on the same exterior edge of the graph,
                //then we actually must render a polygon (w/ 4 points) and take into consideration
                //one of the four corners (either 0,0 or 0,height or width,0 or width,height)
                //note: the 'missing polygon' may have more than just 4 points. this
                //is common when the number of sites are quite low (less than 5), but not a problem
                //with a more useful number of sites. 
                //TODO: find a way to fix this

                if (closeEnough(edgeCorner1.loc.x, edgeCorner2.loc.x, 1)) {
                    drawTriangle(g, edgeCorner1, edgeCorner2, c);
                } else {
                    int[] x = new int[4];
                    int[] y = new int[4];
                    x[0] = (int) c.loc.x;
                    y[0] = (int) c.loc.y;
                    x[1] = (int) edgeCorner1.loc.x;
                    y[1] = (int) edgeCorner1.loc.y;

                    //determine which corner this is
                    x[2] = (int) ((closeEnough(edgeCorner1.loc.x, bounds.x, 1) || closeEnough(edgeCorner2.loc.x, bounds.x, .5)) ? bounds.x : bounds.right);
                    y[2] = (int) ((closeEnough(edgeCorner1.loc.y, bounds.y, 1) || closeEnough(edgeCorner2.loc.y, bounds.y, .5)) ? bounds.y : bounds.bottom);

                    x[3] = (int) edgeCorner2.loc.x;
                    y[3] = (int) edgeCorner2.loc.y;

                    g.fillPolygon(x, y, 4);
                    c.area += 0; //TODO: area of polygon given vertices
                }
            }
        }

        if (drawDelaunay) {
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.YELLOW);
            edges.forEach(e -> g.drawLine((int) e.d0.loc.x, (int) e.d0.loc.y, (int) e.d1.loc.x, (int) e.d1.loc.y));
        }

        if (drawVoronoi) {
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.MAGENTA);
            edges.stream().filter(e -> e.v0 != null && e.v1 != null)
                    .forEach(e -> g.drawLine((int) e.v0.loc.x, (int) e.v0.loc.y, (int) e.v1.loc.x, (int) e.v1.loc.y));
        }

        if (drawRivers) {
            g.setColor(RIVER);
            edges.stream().filter(e -> e.river > 0).forEach(e -> {
                g.setStroke(new BasicStroke(1 + (int) Math.sqrt(e.river * 2)));
                g.drawLine((int) e.v0.loc.x, (int) e.v0.loc.y, (int) e.v1.loc.x, (int) e.v1.loc.y);
            });
        }

        if (drawSites) {
            g.setColor(Color.BLACK);
            centers.forEach(s -> g.fillOval((int) (s.loc.x - 2), (int) (s.loc.y - 2), 4, 4));
        }

        if (drawCorners) {
            g.setColor(Color.WHITE);
            corners.forEach(c -> g.fillOval((int) (c.loc.x - 2), (int) (c.loc.y - 2), 4, 4));
        }
        g.setColor(Color.WHITE);
        g.drawRect((int) bounds.x, (int) bounds.y, (int) bounds.width, (int) bounds.height);
    }

    private void buildGraph(Voronoi v) {
        final Map<Point, Center> pointCenterMap = new HashMap<>();
        final List<Point> points = v.siteCoords();
        for (Point p : points) {
            Center c = new Center(p);
            c.color = new Color(r.nextInt(255), r.nextInt(255), r.nextInt(255));
            centers.add(c);
            pointCenterMap.put(p, c);
        }

        final List<hoten.voronoi.nodename.as3delaunay.Edge> libedges = v.edges();
        final Map<Integer, Corner> pointCornerMap = new HashMap<>();

        for (hoten.voronoi.nodename.as3delaunay.Edge libedge : libedges) {
            final LineSegment vEdge = libedge.voronoiEdge();
            final LineSegment dEdge = libedge.delaunayLine();

            final Edge edge = new Edge();
            edges.add(edge);

            edge.v0 = makeCorner(pointCornerMap, vEdge.p0);
            edge.v1 = makeCorner(pointCornerMap, vEdge.p1);
            edge.d0 = pointCenterMap.get(dEdge.p0);
            edge.d1 = pointCenterMap.get(dEdge.p1);

            // Centers point to edges. Corners point to edges.
            if (edge.d0 != null) {
                edge.d0.borders.add(edge);
            }
            if (edge.d1 != null) {
                edge.d1.borders.add(edge);
            }
            if (edge.v0 != null) {
                edge.v0.protrudes.add(edge);
            }
            if (edge.v1 != null) {
                edge.v1.protrudes.add(edge);
            }

            // Centers point to centers.
            if (edge.d0 != null && edge.d1 != null) {
                addToCenterList(edge.d0.neighbors, edge.d1);
                addToCenterList(edge.d1.neighbors, edge.d0);
            }

            // Corners point to corners
            if (edge.v0 != null && edge.v1 != null) {
                addToCornerList(edge.v0.adjacent, edge.v1);
                addToCornerList(edge.v1.adjacent, edge.v0);
            }

            // Centers point to corners
            if (edge.d0 != null) {
                addToCornerList(edge.d0.corners, edge.v0);
                addToCornerList(edge.d0.corners, edge.v1);
            }
            if (edge.d1 != null) {
                addToCornerList(edge.d1.corners, edge.v0);
                addToCornerList(edge.d1.corners, edge.v1);
            }

            // Corners point to centers
            if (edge.v0 != null) {
                addToCenterList(edge.v0.touches, edge.d0);
                addToCenterList(edge.v0.touches, edge.d1);
            }
            if (edge.v1 != null) {
                addToCenterList(edge.v1.touches, edge.d0);
                addToCenterList(edge.v1.touches, edge.d1);
            }
        }
    }

    // Helper functions for the following for loop; ideally these
    // would be inlined
    private void addToCornerList(List<Corner> list, Corner c) {
        if (c != null && !list.contains(c)) {
            list.add(c);
        }
    }

    private void addToCenterList(List<Center> list, Center c) {
        if (c != null && !list.contains(c)) {
            list.add(c);
        }
    }

    //ensures that each corner is represented by only one corner object
    private Corner makeCorner(Map<Integer, Corner> pointCornerMap, Point p) {
        if (p == null) {
            return null;
        }
        int index = (int) ((int) p.x + (int) (p.y) * bounds.width * 2);
        Corner c = pointCornerMap.get(index);
        if (c == null) {
            c = new Corner();
            c.loc = p;
            c.border = bounds.liesOnAxes(p);
            corners.add(c);
            pointCornerMap.put(index, c);
        }
        return c;
    }

    private void assignCornerElevations() {
        LinkedList<Corner> queue = new LinkedList<>();
        for (Corner c : corners) {
            c.water = isWater(c.loc);
            if (c.border) {
                c.elevation = 0;
                queue.add(c);
            } else {
                c.elevation = Double.MAX_VALUE;
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.adjacent) {
                double newElevation = 0.01 + c.elevation;
                if (!c.water && !a.water) {
                    newElevation += 1;
                }
                if (newElevation < a.elevation) {
                    a.elevation = newElevation;
                    queue.add(a);
                }
            }
        }
    }
    double[][] noise;
    double ISLAND_FACTOR = 1.07;  // 1.0 means no small islands; 2.0 leads to a lot
    final int bumps;
    final double startAngle;
    final double dipAngle;
    final double dipWidth;

    //only the radial implementation of amitp's map generation
    //TODO implement more island shapes
    private boolean isWater(Point p) {
        p = new Point(2 * (p.x / bounds.width - 0.5), 2 * (p.y / bounds.height - 0.5));

        double angle = Math.atan2(p.y, p.x);
        double length = 0.5 * (Math.max(Math.abs(p.x), Math.abs(p.y)) + p.length());

        double r1 = 0.5 + 0.40 * Math.sin(startAngle + bumps * angle + Math.cos((bumps + 3) * angle));
        double r2 = 0.7 - 0.20 * Math.sin(startAngle + bumps * angle - Math.sin((bumps + 2) * angle));
        if (Math.abs(angle - dipAngle) < dipWidth
                || Math.abs(angle - dipAngle + 2 * Math.PI) < dipWidth
                || Math.abs(angle - dipAngle - 2 * Math.PI) < dipWidth) {
            r1 = r2 = 0.2;
        }
        return !(length < r1 || (length > r1 * ISLAND_FACTOR && length < r2));

        //return false;

        /*if (noise == null) {
         noise = new Perlin2d(.125, 8, MyRandom.seed).createArray(257, 257);
         }
         int x = (int) ((p.x + 1) * 128);
         int y = (int) ((p.y + 1) * 128);
         return noise[x][y] < .3 + .3 * p.l2();*/

        /*boolean eye1 = new Point(p.x - 0.2, p.y / 2 + 0.2).length() < 0.05;
         boolean eye2 = new Point(p.x + 0.2, p.y / 2 + 0.2).length() < 0.05;
         boolean body = p.length() < 0.8 - 0.18 * Math.sin(5 * Math.atan2(p.y, p.x));
         return !(body && !eye1 && !eye2);*/
    }

    private void assignOceanCoastAndLand() {
        LinkedList<Center> queue = new LinkedList<>();
        final double waterThreshold = .3;
        for (final Center center : centers) {
            int numWater = 0;
            for (final Corner c : center.corners) {
                if (c.border) {
                    center.border = center.water = center.ocean = true;
                    queue.add(center);
                }
                if (c.water) {
                    numWater++;
                }
            }
            center.water = center.ocean || ((double) numWater / center.corners.size() >= waterThreshold);
        }
        while (!queue.isEmpty()) {
            final Center center = queue.pop();
            for (final Center n : center.neighbors) {
                if (n.water && !n.ocean) {
                    n.ocean = true;
                    queue.add(n);
                }
            }
        }
        for (Center center : centers) {
            boolean oceanNeighbor = center.neighbors.stream().anyMatch(n -> n.ocean);
            boolean landNeighbor = center.neighbors.stream().anyMatch(n -> !n.water);
            center.coast = oceanNeighbor && landNeighbor;
        }

        for (Corner c : corners) {
            long numOcean = c.touches.stream().filter(t -> t.ocean).count();
            long numLand = c.touches.stream().filter(t -> !t.water).count();
            c.ocean = numOcean == c.touches.size();
            c.coast = numOcean > 0 && numLand > 0;
            c.water = c.border || ((numLand != c.touches.size()) && !c.coast);
        }
    }

    private List<Corner> landCorners() {
        return corners.stream().filter(c -> !c.ocean && !c.coast).collect(toList());
    }

    private void redistributeElevations(List<Corner> landCorners) {
        sort(landCorners, comparing(c -> c.elevation));

        final double SCALE_FACTOR = 1.1;
        for (int i = 0; i < landCorners.size(); i++) {
            double y = (double) i / landCorners.size();
            double x = Math.sqrt(SCALE_FACTOR) - Math.sqrt(SCALE_FACTOR * (1 - y));
            x = Math.min(x, 1);
            landCorners.get(i).elevation = x;
        }

        corners.stream().filter(c -> c.ocean || c.coast).forEach(c -> c.elevation = 0.0);
    }

    private void assignPolygonElevations() {
        centers.forEach(ce -> ce.elevation = ce.corners.stream().collect(averagingDouble(co -> co.elevation)));
    }

    private void calculateDownslopes() {
        corners.forEach(c -> c.downslope = c.adjacent.stream().collect(minBy(comparing(adj -> adj.elevation))).get());
    }

    private void createRivers() {
        for (int i = 0; i < bounds.width / 2; i++) {
            Corner c = corners.get(r.nextInt(corners.size()));
            if (c.ocean || c.elevation < 0.3 || c.elevation > 0.9) {
                continue;
            }
            // Bias rivers to go west: if (q.downslope.x > q.x) continue;
            while (!c.coast) {
                if (c == c.downslope) {
                    break;
                }
                Edge edge = lookupEdgeFromCorner(c, c.downslope);
                if (!edge.v0.water || !edge.v1.water) {
                    edge.river++;
                    c.river++;
                    c.downslope.river++;  // TODO: fix double count
                }
                c = c.downslope;
            }
        }
    }

    private Edge lookupEdgeFromCorner(Corner c, Corner downslope) {
        return c.protrudes.stream().filter(e -> e.v0 == downslope || e.v1 == downslope).findFirst().orElse(null);
    }

    private void assignCornerMoisture() {
        LinkedList<Corner> queue = new LinkedList<>();
        for (Corner c : corners) {
            if ((c.water || c.river > 0) && !c.ocean) {
                c.moisture = c.river > 0 ? Math.min(3.0, (0.2 * c.river)) : 1.0;
                queue.push(c);
            } else {
                c.moisture = 0.0;
            }
        }

        while (!queue.isEmpty()) {
            Corner c = queue.pop();
            for (Corner a : c.adjacent) {
                double newM = .9 * c.moisture;
                if (newM > a.moisture) {
                    a.moisture = newM;
                    queue.add(a);
                }
            }
        }

        corners.stream().filter(c -> c.ocean || c.coast).forEach(c -> c.moisture = 1.0);
    }

    private void redistributeMoisture(List<Corner> landCorners) {
        sort(landCorners, comparing(c -> c.moisture));
        for (int i = 0; i < landCorners.size(); i++) {
            landCorners.get(i).moisture = (double) i / landCorners.size();
        }
    }

    private void assignPolygonMoisture() {
        centers.forEach(ce -> ce.moisture = ce.corners.stream().collect(averagingDouble(co -> co.moisture)));
    }

    private void assignBiomes() {
        centers.forEach(center -> center.biome = getBiome(center));
    }
}

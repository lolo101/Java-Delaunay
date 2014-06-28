package hoten.voronoi.nodename.as3delaunay;

/*
 * Java implementaition by Connor Clark (www.hotengames.com). Pretty much a 1:1 
 * translation of a wonderful map generating algorthim by Amit Patel of Red Blob Games,
 * which can be found here (http://www-cs-students.stanford.edu/~amitp/game-programming/polygon-map-generation/)
 * Hopefully it's of use to someone out there who needed it in Java like I did!
 * Note, the only island mode implemented is Radial. Implementing more is something for another day.
 * 
 * FORTUNE'S ALGORTIHIM
 * 
 * This is a java implementation of an AS3 (Flash) implementation of an algorthim
 * originally created in C++. Pretty much a 1:1 translation from as3 to java, save
 * for some necessary workarounds. Original as3 implementation by Alan Shaw (of nodename)
 * can be found here (https://github.com/nodename/as3delaunay). Original algorthim
 * by Steven Fortune (see lisence for c++ implementation below)
 * 
 * The author of this software is Steven Fortune.  Copyright (c) 1994 by AT&T
 * Bell Laboratories.
 * Permission to use, copy, modify, and distribute this software for any
 * purpose without fee is hereby granted, provided that this entire notice
 * is included in all copies of any software which is or includes a copy
 * or modification of this software and in all copies of the supporting
 * documentation for such software.
 * THIS SOFTWARE IS BEING PROVIDED "AS IS", WITHOUT ANY EXPRESS OR IMPLIED
 * WARRANTY.  IN PARTICULAR, NEITHER THE AUTHORS NOR AT&T MAKE ANY
 * REPRESENTATION OR WARRANTY OF ANY KIND CONCERNING THE MERCHANTABILITY
 * OF THIS SOFTWARE OR ITS FITNESS FOR ANY PARTICULAR PURPOSE.
 */
import hoten.geom.Point;
import hoten.geom.Rectangle;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Comparator.naturalOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.toList;

public final class Voronoi {

    private SiteList _sites;
    private HashMap<Point, Site> _sitesIndexedByLocation;
    private final List<Triangle> _triangles = new ArrayList<>();
    private final List<Edge> _edges = new ArrayList<>();
    // TODO generalize this so it doesn't have to be a rectangle;
    // then we can make the fractal voronois-within-voronois
    private Rectangle _plotBounds;

    public Rectangle get_plotBounds() {
        return _plotBounds;
    }

    public void dispose() {
        int i, n;
        if (_sites != null) {
            _sites.dispose();
            _sites = null;
        }
        if (_triangles != null) {
            n = _triangles.size();
            for (i = 0; i < n; ++i) {
                _triangles.get(i).dispose();
            }
            _triangles.clear();
        }
        if (_edges != null) {
            n = _edges.size();
            for (i = 0; i < n; ++i) {
                _edges.get(i).dispose();
            }
            _edges.clear();
        }
        _plotBounds = null;
        _sitesIndexedByLocation = null;
    }

    public Voronoi(List<Point> points, List<Color> colors, Rectangle plotBounds) {
        init(points, colors, plotBounds);
        fortunesAlgorithm();
    }

    public Voronoi(List<Point> points, List<Color> colors) {
        this(points, colors, new Rectangle(0, 0,
                points.stream().map(p -> p.x).collect(maxBy(naturalOrder())).get(),
                points.stream().map(p -> p.y).collect(maxBy(naturalOrder())).get()));
    }

    public Voronoi(int numSites, double maxWidth, double maxHeight, Random r, List<Color> colors) {
        List<Point> points = new ArrayList<>(numSites);
        for (int i = 0; i < numSites; i++) {
            points.add(new Point(r.nextDouble() * maxWidth, r.nextDouble() * maxHeight));
        }
        init(points, colors, new Rectangle(0, 0, maxWidth, maxHeight));
        fortunesAlgorithm();
    }

    private void init(List<Point> points, List<Color> colors, Rectangle plotBounds) {
        _sites = new SiteList();
        _sitesIndexedByLocation = new HashMap<>();
        addSites(points, colors);
        _plotBounds = plotBounds;
    }

    private void addSites(List<Point> points, List<Color> colors) {
        int length = points.size();
        for (int i = 0; i < length; ++i) {
            addSite(points.get(i), colors != null ? colors.get(i) : null, i);
        }
    }

    private void addSite(Point p, Color color, int index) {
        double weight = Math.random() * 100;
        Site site = Site.create(p, index, weight, color);
        _sites.push(site);
        _sitesIndexedByLocation.put(p, site);
    }

    public List<Edge> edges() {
        return _edges;
    }

    /**
     * Returns the region of the site specified by the site's exact coordinates.
     * @param p
     * @return
     */
    public List<Point> region(Point p) {
        Site site = _sitesIndexedByLocation.get(p);
        if (site == null) {
            return Collections.emptyList();
        }
        return site.region(_plotBounds);
    }

    // TODO: bug: if you call this before you call region(), something goes wrong :(
    public List<Point> neighborSitesForSite(Point coord) {
        Site site = _sitesIndexedByLocation.get(coord);
        if (site == null) {
            return Collections.emptyList();
        }
        List<Site> sites = site.neighborSites();
        return sites.stream().map(Site::get_coord).collect(toList());
    }

    public List<Circle> circles() {
        return _sites.circles();
    }

    private List<Edge> selectEdgesForSitePoint(Point coord, List<Edge> edgesToTest) {
        List<Edge> filtered = new ArrayList<>();

        for (Edge e : edgesToTest) {
            if (((e.get_leftSite() != null && e.get_leftSite().get_coord() == coord)
                    || (e.get_rightSite() != null && e.get_rightSite().get_coord() == coord))) {
                filtered.add(e);
            }
        }
        return filtered;

        /*function myTest(edge:Edge, index:int, vector:Vector.<Edge>):Boolean
         {
         return ((edge.leftSite && edge.leftSite.coord == coord)
         ||  (edge.rightSite && edge.rightSite.coord == coord));
         }*/
    }

    private List<LineSegment> visibleLineSegments(List<Edge> edges) {
        List<LineSegment> segments = new ArrayList<>();

        for (Edge edge : edges) {
            if (edge.get_visible()) {
                Point p1 = edge.get_clippedEnds().get(LR.LEFT);
                Point p2 = edge.get_clippedEnds().get(LR.RIGHT);
                segments.add(new LineSegment(p1, p2));
            }
        }

        return segments;
    }

    private List<LineSegment> delaunayLinesForEdges(List<Edge> edges) {
        return edges.stream().map(Edge::delaunayLine).collect(toList());
    }

    public List<LineSegment> voronoiBoundaryForSite(Point coord) {
        return visibleLineSegments(selectEdgesForSitePoint(coord, _edges));
    }

    public List<LineSegment> delaunayLinesForSite(Point coord) {
        return delaunayLinesForEdges(selectEdgesForSitePoint(coord, _edges));
    }

    public List<LineSegment> voronoiDiagram() {
        return visibleLineSegments(_edges);
    }

    public List<LineSegment> hull() {
        return delaunayLinesForEdges(hullEdges());
    }

    private List<Edge> hullEdges() {
        List<Edge> filtered = new ArrayList<>();

        for (Edge e : _edges) {
            if (e.isPartOfConvexHull()) {
                filtered.add(e);
            }
        }



        return filtered;

        /*function myTest(edge:Edge, index:int, vector:Vector.<Edge>):Boolean
         {
         return (edge.isPartOfConvexHull());
         }*/
    }

    public List<Point> hullPointsInOrder() {
        List<Edge> hullEdges = hullEdges();

        List<Point> points = new ArrayList<>();
        if (hullEdges.isEmpty()) {
            return points;
        }

        EdgeReorderer reorderer = new EdgeReorderer(hullEdges, Site.class);
        hullEdges = reorderer.get_edges();
        List<LR> orientations = reorderer.get_edgeOrientations();
        reorderer.dispose();

        LR orientation;

        int n = hullEdges.size();
        for (int i = 0; i < n; ++i) {
            Edge edge = hullEdges.get(i);
            orientation = orientations.get(i);
            points.add(edge.site(orientation).get_coord());
        }
        return points;
    }

    public List<List<Point>> regions() {
        return _sites.regions(_plotBounds);
    }

    public List<Point> siteCoords() {
        return _sites.siteCoords();
    }

    private void fortunesAlgorithm() {
        Site newSite, bottomSite, topSite, tempSite;
        Vertex v, vertex;
        Point newintstar = null;
        Halfedge lbnd, rbnd, llbnd, rrbnd, bisector;
        Edge edge;

        Rectangle dataBounds = _sites.getSitesBounds();

        int sqrt_nsites = (int) Math.sqrt(_sites.get_length() + 4);
        HalfedgePriorityQueue heap = new HalfedgePriorityQueue(dataBounds.y, dataBounds.height, sqrt_nsites);
        EdgeList edgeList = new EdgeList(dataBounds.x, dataBounds.width, sqrt_nsites);
        List<Halfedge> halfEdges = new ArrayList<>();
        List<Vertex> vertices = new ArrayList<>();

        Site bottomMostSite = _sites.next();
        newSite = _sites.next();

        for (;;) {
            if (heap.empty() == false) {
                newintstar = heap.min();
            }

            if (newSite != null
                    && (heap.empty() || compareByYThenX(newSite, newintstar) < 0)) {
                /* new site is smallest */
                //trace("smallest: new site " + newSite);

                // Step 8:
                lbnd = edgeList.edgeListLeftNeighbor(newSite.get_coord());	// the Halfedge just to the left of newSite
                //trace("lbnd: " + lbnd);
                rbnd = lbnd.edgeListRightNeighbor;		// the Halfedge just to the right
                //trace("rbnd: " + rbnd);
                bottomSite = rightRegion(lbnd, bottomMostSite);		// this is the same as leftRegion(rbnd)
                // this Site determines the region containing the new site
                //trace("new Site is in region of existing site: " + bottomSite);

                // Step 9:
                edge = Edge.createBisectingEdge(bottomSite, newSite);
                //trace("new edge: " + edge);
                _edges.add(edge);

                bisector = Halfedge.create(edge, LR.LEFT);
                halfEdges.add(bisector);
                // inserting two Halfedges into edgeList constitutes Step 10:
                // insert bisector to the right of lbnd:
                edgeList.insert(lbnd, bisector);

                // first half of Step 11:
                if ((vertex = Vertex.intersect(lbnd, bisector)) != null) {
                    vertices.add(vertex);
                    heap.remove(lbnd);
                    lbnd.vertex = vertex;
                    lbnd.ystar = vertex.get_y() + newSite.dist(vertex);
                    heap.insert(lbnd);
                }

                lbnd = bisector;
                bisector = Halfedge.create(edge, LR.RIGHT);
                halfEdges.add(bisector);
                // second Halfedge for Step 10:
                // insert bisector to the right of lbnd:
                edgeList.insert(lbnd, bisector);

                // second half of Step 11:
                if ((vertex = Vertex.intersect(bisector, rbnd)) != null) {
                    vertices.add(vertex);
                    bisector.vertex = vertex;
                    bisector.ystar = vertex.get_y() + newSite.dist(vertex);
                    heap.insert(bisector);
                }

                newSite = _sites.next();
            } else if (heap.empty() == false) {
                /* intersection is smallest */
                lbnd = heap.extractMin();
                llbnd = lbnd.edgeListLeftNeighbor;
                rbnd = lbnd.edgeListRightNeighbor;
                rrbnd = rbnd.edgeListRightNeighbor;
                bottomSite = leftRegion(lbnd, bottomMostSite);
                topSite = rightRegion(rbnd, bottomMostSite);
                // these three sites define a Delaunay triangle
                // (not actually using these for anything...)
                //_triangles.push(new Triangle(bottomSite, topSite, rightRegion(lbnd)));

                v = lbnd.vertex;
                v.setIndex();
                lbnd.edge.setVertex(lbnd.leftRight, v);
                rbnd.edge.setVertex(rbnd.leftRight, v);
                edgeList.remove(lbnd);
                heap.remove(rbnd);
                edgeList.remove(rbnd);
                LR leftRight = LR.LEFT;
                if (bottomSite.get_y() > topSite.get_y()) {
                    tempSite = bottomSite;
                    bottomSite = topSite;
                    topSite = tempSite;
                    leftRight = LR.RIGHT;
                }
                edge = Edge.createBisectingEdge(bottomSite, topSite);
                _edges.add(edge);
                bisector = Halfedge.create(edge, leftRight);
                halfEdges.add(bisector);
                edgeList.insert(llbnd, bisector);
                edge.setVertex(leftRight.other(), v);
                if ((vertex = Vertex.intersect(llbnd, bisector)) != null) {
                    vertices.add(vertex);
                    heap.remove(llbnd);
                    llbnd.vertex = vertex;
                    llbnd.ystar = vertex.get_y() + bottomSite.dist(vertex);
                    heap.insert(llbnd);
                }
                if ((vertex = Vertex.intersect(bisector, rrbnd)) != null) {
                    vertices.add(vertex);
                    bisector.vertex = vertex;
                    bisector.ystar = vertex.get_y() + bottomSite.dist(vertex);
                    heap.insert(bisector);
                }
            } else {
                break;
            }
        }

        // heap should be empty now
        heap.dispose();
        edgeList.dispose();

        for (Halfedge halfEdge : halfEdges) {
            halfEdge.reallyDispose();
        }
        halfEdges.clear();

        // we need the vertices to clip the edges
        for (Edge e : _edges) {
            e.clipVertices(_plotBounds);
        }
        // but we don't actually ever use them again!
        for (Vertex v0 : vertices) {
            v0.dispose();
        }
        vertices.clear();


    }

    Site leftRegion(Halfedge he, Site bottomMostSite) {
        Edge edge = he.edge;
        if (edge == null) {
            return bottomMostSite;
        }
        return edge.site(he.leftRight);
    }

    Site rightRegion(Halfedge he, Site bottomMostSite) {
        Edge edge = he.edge;
        if (edge == null) {
            return bottomMostSite;
        }
        return edge.site(he.leftRight.other());
    }

    public static int compareByYThenX(Site s1, Site s2) {
        if (s1.get_y() < s2.get_y()) {
            return -1;
        }
        if (s1.get_y() > s2.get_y()) {
            return 1;
        }
        if (s1.get_x() < s2.get_x()) {
            return -1;
        }
        if (s1.get_x() > s2.get_x()) {
            return 1;
        }
        return 0;
    }

    public static int compareByYThenX(Site s1, Point s2) {
        if (s1.get_y() < s2.y) {
            return -1;
        }
        if (s1.get_y() > s2.y) {
            return 1;
        }
        if (s1.get_x() < s2.x) {
            return -1;
        }
        if (s1.get_x() > s2.x) {
            return 1;
        }
        return 0;
    }
}

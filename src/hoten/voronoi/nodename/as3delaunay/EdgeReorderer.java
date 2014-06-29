package hoten.voronoi.nodename.as3delaunay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EdgeReorderer {

    private List<Edge> _edges;
    private List<LR> _edgeOrientations;

    public List<Edge> get_edges() {
        return _edges;
    }

    public List<LR> get_edgeOrientations() {
        return _edgeOrientations;
    }

    public EdgeReorderer(List<Edge> origEdges, Class<?> criterion) {
        if (criterion != Vertex.class && criterion != Site.class) {
            throw new Error("Edges: criterion must be Vertex or Site");
        }
        _edges = new ArrayList<>();
        _edgeOrientations = new ArrayList<>();
        if (origEdges.size() > 0) {
            _edges = reorderEdges(origEdges, criterion);
        }
    }

    public void dispose() {
        _edges = null;
        _edgeOrientations = null;
    }

    private List<Edge> reorderEdges(List<Edge> origEdges, Class<?> criterion) {
        int n = origEdges.size();
        // we're going to reorder the edges in order of traversal
        List<Boolean> done = new ArrayList<>(n);
        int nDone = 0;
        for (int k = 0; k < n; k++) {
            done.add( false);
        }
        List<Edge> newEdges = new ArrayList<>(n);

        int i = 0;
        Edge edge = origEdges.get(i);
        newEdges.add(edge);
        _edgeOrientations.add(LR.LEFT);
        ICoord firstPoint = (criterion == Vertex.class) ? edge.get_leftVertex() : edge.get_leftSite();
        ICoord lastPoint = (criterion == Vertex.class) ? edge.get_rightVertex() : edge.get_rightSite();

        if (firstPoint == Vertex.VERTEX_AT_INFINITY || lastPoint == Vertex.VERTEX_AT_INFINITY) {
            return Collections.emptyList();
        }

        done.set(i, true);
        ++nDone;

        while (nDone < n) {
            for (i = 1; i < n; ++i) {
                if (done.get(i)) {
                    continue;
                }
                edge = origEdges.get(i);
                ICoord leftPoint = (criterion == Vertex.class) ? edge.get_leftVertex() : edge.get_leftSite();
                ICoord rightPoint = (criterion == Vertex.class) ? edge.get_rightVertex() : edge.get_rightSite();
                if (leftPoint == Vertex.VERTEX_AT_INFINITY || rightPoint == Vertex.VERTEX_AT_INFINITY) {
                    return Collections.emptyList();
                }
                if (leftPoint == lastPoint) {
                    lastPoint = rightPoint;
                    _edgeOrientations.add(LR.LEFT);
                    newEdges.add(edge);
                    done.set(i, true);
                } else if (rightPoint == firstPoint) {
                    firstPoint = leftPoint;
                    _edgeOrientations.add(0, LR.LEFT);
                    newEdges.add(0, edge);
                    done.set(i, true);
                } else if (leftPoint == firstPoint) {
                    firstPoint = rightPoint;
                    _edgeOrientations.add(0, LR.RIGHT);
                    newEdges.add(0, edge);

                    done.set(i, true);
                } else if (rightPoint == lastPoint) {
                    lastPoint = leftPoint;
                    _edgeOrientations.add(LR.RIGHT);
                    newEdges.add(edge);
                    done.set(i, true);
                }
                if (done.get(i)) {
                    ++nDone;
                }
            }
        }

        return newEdges;
    }
}
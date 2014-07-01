package hoten.voronoi;

import hoten.geom.Point;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Center.java
 *
 * @author Connor
 */
public class Center {

    public Color color;
    public final Point loc;
    public final List<Corner> corners = new ArrayList<>();//good
    public final List<Center> neighbors = new ArrayList<>();//good
    public final List<Edge> borders = new ArrayList<>();
    public boolean border, ocean, water, coast;
    public double elevation;
    public double moisture;
    public Enum biome;
    public double area;

    public Center(Point loc) {
        this.loc = loc;
    }
}

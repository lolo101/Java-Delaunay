package hoten.voronoi.nodename.as3delaunay;

public enum LR {

    LEFT,
    RIGHT;

    public LR other() {
        return this == LEFT ? RIGHT : LEFT;
    }
}

package hoten.voronoi.nodename.as3delaunay;

public enum LR {

    LEFT,
    RIGHT;

    public static LR other(LR leftRight) {
        return leftRight == LEFT ? RIGHT : LEFT;
    }
}

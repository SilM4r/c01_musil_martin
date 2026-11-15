package model;

/** Hrana polygonu pro Scan-Line. */
public class Edge {
    private final Point2D p1;
    private final Point2D p2;

    public Edge(Point2D p1, Point2D p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public boolean isHorizontal() {
        return p1.getY() == p2.getY();
    }

    // otočí hranu tak, aby vedla shora dolů (y1 < y2)
    public Edge orientate() {
        if (p1.getY() > p2.getY()) {
            return new Edge(p2, p1);
        }
        return this;
    }

    public int getY1() {
        return p1.getY();
    }

    public int getY2() {
        return p2.getY();
    }

    // spodní konec se nepočítá, jinak by se společný vrchol dvou hran započítal dvakrát
    public boolean hasIntersection(int y) {
        return y >= p1.getY() && y < p2.getY();
    }

    public int getIntersection(int y) {
        double k = (double) (p2.getX() - p1.getX()) / (p2.getY() - p1.getY());
        return (int) Math.round(p1.getX() + k * (y - p1.getY()));
    }
}

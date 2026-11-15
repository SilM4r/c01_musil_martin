package model;

public class Line {
    private final Point2D start, end;
    private final int color;

    public Line(Point2D start, Point2D end, int color) {
        this.start = start;
        this.end = end;
        this.color = color;
    }

    public Point2D getStart() {
        return start;
    }

    public Point2D getEnd() {
        return end;
    }

    public int getColor() {
        return color;
    }
}

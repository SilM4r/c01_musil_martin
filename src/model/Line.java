package model;

/** Úsečka zadaná počátečním a koncovým bodem a barvou. */
public class Line {
    private final Point2D start, end;
    private final int color;

    public Line(int startX, int startY, int endX, int endY) {
        this(new Point2D(startX, startY), new Point2D(endX, endY), Context.Color.COLOR);
    }

    public Line(Point2D start, Point2D end) {
        this(start, end, Context.Color.COLOR);
    }

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

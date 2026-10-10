package model;

public class Line {
    private final Point2D start,end;

    public Point2D getStart(){
        return start;
    }

    public Point2D getEnd(){
        return end;
    }

    public Line(int startX, int startY, int endX, int endY) {
        start = new Point2D(startX,startY);
        end = new Point2D(endX,endY);
    }

    public Line(Point2D start,Point2D end) {
        this.start = start;
        this.end = end;
    }
}

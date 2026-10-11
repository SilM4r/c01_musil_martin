package model;

import java.util.ArrayList;
import java.util.List;

/** Uzavřený polygon zadaný seznamem vrcholů. */
public class Polygon {
    private final List<Point2D> points;

    public Polygon() {
        points = new ArrayList<>();
    }

    public void addPoint(Point2D point) {
        points.add(point);
    }

    public Point2D getPoint(int index) {
        return points.get(index);
    }

    public void setPoint(int index, Point2D point) {
        points.set(index, point);
    }

    public int getSize() {
        return points.size();
    }

    public void clear() {
        points.clear();
    }

    public void remove(int index) {
        points.remove(index);
    }

    // index nejbližšího vrcholu v okruhu range, jinak -1
    public int nearestPoint(Point2D point, int range) {
        if (points.isEmpty())
            return -1;

        double minDistance = Double.MAX_VALUE;
        int minDistancePointIndex = 0;
        for (int i = 0; i < points.size(); i++) {
            Point2D polygonPoint = points.get(i);
            double distance = pythagoras(polygonPoint.getX() - point.getX(), polygonPoint.getY() - point.getY());

            if (distance < minDistance) {
                minDistance = distance;
                minDistancePointIndex = i;
            }
        }

        if (minDistance <= range) {
            return minDistancePointIndex;
        } else {
            return -1;
        }
    }

    // vzdálenost dvou bodů z rozdílů jejich souřadnic
    private double pythagoras(int a, int b) {
        return Math.sqrt(a * a + b * b);
    }
}

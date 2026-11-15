package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon {
    private final List<Point2D> points;
    private int color = Context.Color.COLOR;

    public Polygon() {
        points = new ArrayList<>();
    }

    public List<Point2D> getPoints() {
        return points;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
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

    // plocha se znaménkem (shoelace); osa y míří dolů, proto kladná = po směru hodinových ručiček
    public double getSignedArea() {
        if (points.size() < 3) {
            return 0;
        }
        double area = 0;
        for (int i = 0; i < points.size(); i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get((i + 1) % points.size());
            area += (double) p1.getX() * p2.getY() - (double) p2.getX() * p1.getY();
        }
        return area / 2.0;
    }

    public boolean isCW() {
        return getSignedArea() > 0;
    }

    // kopie vrcholů proti směru hodinových ručiček, polygon sám se nemění
    public List<Point2D> getPointsCCW() {
        List<Point2D> result = new ArrayList<>(points);
        if (result.size() >= 3 && isCW()) {
            Collections.reverse(result);
        }
        return result;
    }

    // [minX, minY, maxX, maxY], pro prázdný polygon null
    public double[] getBoundingBox() {
        if (points.isEmpty()) {
            return null;
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (Point2D p : points) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getY() > maxY) maxY = p.getY();
        }
        return new double[]{minX, minY, maxX, maxY};
    }

    // 4 vrcholy a každá hrana vodorovná nebo svislá; tolerance 2 px kvůli kreslení myší
    public boolean isAxisAlignedRectangle() {
        if (points.size() != 4) {
            return false;
        }
        for (int i = 0; i < 4; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get((i + 1) % 4);
            boolean isHorizontal = Math.abs(p1.getY() - p2.getY()) <= 2;
            boolean isVertical = Math.abs(p1.getX() - p2.getX()) <= 2;
            if (!isHorizontal && !isVertical) {
                return false;
            }
        }
        return true;
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

    private double pythagoras(int a, int b) {
        return Math.sqrt(a * a + b * b);
    }
}

package rasterize;

import model.Point2D;
import model.Polygon;

public class PolygonRasterizer {

    private final LineRasterizer lineRasterizer;

    public PolygonRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void rasterize(Polygon polygon) {
        int previousColor = lineRasterizer.getColor();
        lineRasterizer.setColor(polygon.getColor());

        for (int i = 0; i < polygon.getSize(); i++) {
            Point2D pointA = polygon.getPoint(i);
            // poslední vrchol se spojí s prvním, polygon je vždy uzavřený
            Point2D pointB = polygon.getPoint((i + 1) % polygon.getSize());
            lineRasterizer.rasterize(pointA, pointB);
        }

        lineRasterizer.setColor(previousColor);
    }
}

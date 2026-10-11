package rasterize;

import model.Point2D;
import model.Polygon;

/** Kreslí obvod polygonu – spojí každý vrchol s následujícím, poslední s prvním. */
public class PolygonRasterizer {

    private LineRasterizer lineRasterizer;

    public PolygonRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void setLineRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void rasterize(Polygon polygon) {
        if (polygon.getSize() == 1) {
            Point2D pointA = polygon.getPoint(0);
            lineRasterizer.rasterize(pointA, pointA);
        } else if (polygon.getSize() == 2) {
            Point2D pointA = polygon.getPoint(0);
            Point2D pointB = polygon.getPoint(1);
            lineRasterizer.rasterize(pointA, pointB);
        }

        for (int i = 0; i < polygon.getSize(); i++) {
            Point2D pointA = polygon.getPoint(i);
            // u posledního vrcholu se vrátí na první a polygon se uzavře
            Point2D pointB = polygon.getPoint((i + 1) % polygon.getSize());
            lineRasterizer.rasterize(pointA, pointB);
        }
    }
}

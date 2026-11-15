package Filler;

import model.Edge;
import model.Point2D;
import model.Polygon;
import raster.Raster;
import rasterize.LineRasterizer;
import rasterize.PolygonRasterizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Vyplňování polygonu po řádcích podle průsečíků s hranami (pravidlo sudá–lichá). */
public class ScanLineFill implements Filler {
    private final Raster raster;
    private final PolygonRasterizer polygonRasterizer;
    private int fillColor = Context.Color.FILL_COLOR;
    private Polygon polygon;

    public ScanLineFill(LineRasterizer lineRasterizer, PolygonRasterizer polygonRasterizer) {
        this.raster = lineRasterizer.getRaster();
        this.polygonRasterizer = polygonRasterizer;
    }

    @Override
    public void fill() {
        if (polygon == null || polygon.getSize() < 3) {
            return;
        }

        // vodorovné hrany se vynechají, řádek s nimi nemá jediný průsečík
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < polygon.getSize(); i++) {
            Point2D p1 = polygon.getPoint(i);
            Point2D p2 = polygon.getPoint((i + 1) % polygon.getSize());
            Edge edge = new Edge(p1, p2);
            if (!edge.isHorizontal()) {
                edges.add(edge.orientate());
            }
        }

        if (edges.isEmpty()) {
            return;
        }

        int minY = edges.get(0).getY1();
        int maxY = edges.get(0).getY2();
        for (Edge edge : edges) {
            if (edge.getY1() < minY) minY = edge.getY1();
            if (edge.getY2() > maxY) maxY = edge.getY2();
        }

        int startY = Math.max(0, minY);
        int endY = Math.min(raster.getHeight() - 1, maxY);

        for (int y = startY; y <= endY; y++) {
            List<Integer> intersections = new ArrayList<>();
            for (Edge edge : edges) {
                if (edge.hasIntersection(y)) {
                    intersections.add(edge.getIntersection(y));
                }
            }
            Collections.sort(intersections);

            // vyplní se úseky mezi 1. a 2., 3. a 4. … průsečíkem
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                drawHorizontalLine(intersections.get(i), intersections.get(i + 1), y);
            }
        }

        // výplň zčásti překryje obrys, proto se obtáhne znovu
        polygonRasterizer.rasterize(polygon);
    }

    private void drawHorizontalLine(int x1, int x2, int y) {
        int startX = Math.max(0, x1);
        int endX = Math.min(raster.getWidth() - 1, x2);
        for (int x = startX; x <= endX; x++) {
            raster.setPixel(x, y, fillColor);
        }
    }

    @Override
    public void setColor(int color) {
        this.fillColor = color;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }
}

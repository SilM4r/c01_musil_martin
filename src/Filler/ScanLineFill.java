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

/**
 * Algoritmus Scan-Line pro vyplňování polygonu (n-úhelníku).
 * Prochází řádek po řádku, hledá průsečíky s orientovanými hranami,
 * seřadí je a vyplňuje pixely mezi sudými a lichými průsečíky.
 */
public class ScanLineFill implements Filler {
    private int fillColor;
    private Polygon polygon;
    private Raster raster;
    private LineRasterizer lineRasterizer;
    private PolygonRasterizer polygonRasterizer;

    public ScanLineFill() {
        setColor(Filler.COLOR);
    }

    public ScanLineFill(LineRasterizer lineRasterizer, PolygonRasterizer polygonRasterizer) {
        this();
        this.lineRasterizer = lineRasterizer;
        this.polygonRasterizer = polygonRasterizer;
        if (lineRasterizer != null) {
            this.raster = lineRasterizer.getRaster();
        }
    }

    @Override
    public void fill() {
        if (polygon == null || polygon.getSize() < 3) {
            return;
        }

        // 1. Z polygonu vytvoříme orientované hrany bez vodorovných
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

        // 2. Nalezení minimálního a maximálního Y
        int minY = edges.get(0).getY1();
        int maxY = edges.get(0).getY2();
        for (Edge edge : edges) {
            if (edge.getY1() < minY) minY = edge.getY1();
            if (edge.getY2() > maxY) maxY = edge.getY2();
        }

        Raster r = getEffectiveRaster();

        int startY = minY;
        int endY = maxY;
        if (r != null) {
            startY = Math.max(0, minY);
            endY = Math.min(r.getHeight() - 1, maxY);
        }

        // 3. Procházení jednotlivých scanline řádků
        for (int y = startY; y <= endY; y++) {
            List<Integer> intersections = new ArrayList<>();

            for (Edge edge : edges) {
                if (edge.hasIntersection(y)) {
                    intersections.add(edge.getIntersection(y));
                }
            }

            // 4. Seřazení průsečíků vzestupně podle X
            Collections.sort(intersections);

            // 5. Spojení dvojic průsečíků vodorovnou čarou (pravidlo sudá-lichá)
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int x1 = intersections.get(i);
                int x2 = intersections.get(i + 1);
                int startX = Math.min(x1, x2);
                int endX = Math.max(x1, x2);

                drawHorizontalLine(startX, endX, y);
            }
        }

        // 6. Obtažení hranice polygonu pro vyhlazení okrajů
        if (polygonRasterizer != null) {
            polygonRasterizer.rasterize(polygon);
        } else if (lineRasterizer != null) {
            new PolygonRasterizer(lineRasterizer).rasterize(polygon);
        }
    }

    private Raster getEffectiveRaster() {
        if (this.raster != null) {
            return this.raster;
        }
        if (this.lineRasterizer != null) {
            return this.lineRasterizer.getRaster();
        }
        return null;
    }

    private void drawHorizontalLine(int x1, int x2, int y) {
        Raster r = getEffectiveRaster();
        if (r != null) {
            if (y < 0 || y >= r.getHeight()) {
                return;
            }
            int startX = Math.max(0, x1);
            int endX = Math.min(r.getWidth() - 1, x2);
            for (int x = startX; x <= endX; x++) {
                r.setPixel(x, y, fillColor);
            }
        } else if (lineRasterizer != null) {
            lineRasterizer.rasterize(x1, y, x2, y);
        }
    }

    @Override
    public void setColor(int color) {
        this.fillColor = color;
    }

    public int getColor() {
        return fillColor;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setLineRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
        if (this.raster == null && lineRasterizer != null) {
            this.raster = lineRasterizer.getRaster();
        }
    }

    public LineRasterizer getLineRasterizer() {
        return lineRasterizer;
    }

    public void setPolygonRasterizer(PolygonRasterizer polygonRasterizer) {
        this.polygonRasterizer = polygonRasterizer;
    }

    public PolygonRasterizer getPolygonRasterizer() {
        return polygonRasterizer;
    }
}

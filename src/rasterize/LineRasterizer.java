package rasterize;

import model.Line;
import model.Point2D;
import raster.Raster;

/** Společný předek algoritmů pro kreslení úsečky. */
public abstract class LineRasterizer {
    protected Raster raster;

    public LineRasterizer(Raster raster) {
        this.raster = raster;
    }

    public abstract void rasterize(int x1, int y1, int x2, int y2);

    public void rasterize(Point2D a, Point2D b) {
        rasterize(a.getX(), a.getY(), b.getX(), b.getY());
    }

    public void rasterize(Line line) {
        rasterize(line.getStart(), line.getEnd());
    }
}

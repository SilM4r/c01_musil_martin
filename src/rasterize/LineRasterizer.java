package rasterize;

import model.Line;
import model.Point2D;
import raster.Raster;

public abstract class LineRasterizer {
    protected Raster raster;
    protected int color = Context.Color.COLOR;

    public LineRasterizer(Raster raster) {
        this.raster = raster;
    }

    public Raster getRaster() {
        return raster;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public abstract void rasterize(int x1, int y1, int x2, int y2);

    public void rasterize(Point2D a, Point2D b) {
        rasterize(a.getX(), a.getY(), b.getX(), b.getY());
    }

    // úsečka se kreslí vlastní barvou, barva rasterizéru se pak vrátí
    public void rasterize(Line line) {
        int previousColor = this.color;
        this.color = line.getColor();
        rasterize(line.getStart(), line.getEnd());
        this.color = previousColor;
    }
}

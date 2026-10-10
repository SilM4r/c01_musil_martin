package rasterize;

import Context.Color;
import model.Line;
import model.Point2D;
import raster.Raster;

public class LineRasterizerTrivial extends LineRasterizer {

    int color = Color.COLOR;

    public LineRasterizerTrivial(Raster raster) {
        super(raster);
    }

    @Override
    public void rasterize(Point2D pointA, Point2D pointB){
        rasterize(pointA.getX(),pointA.getY(),pointB.getX(),pointB.getY());

    }

    @Override
    public void rasterize(Line line){
        rasterize(line.getStart().getX(),line.getStart().getY(),line.getEnd().getX(),line.getEnd().getY());

    }

    @Override
    public void rasterize(int x1, int y1, int x2, int y2) {
        if (x1 == x2) {
            if (y1 > y2) {
                int t = y1; y1 = y2; y2 = t;
            }
            for (int y = y1; y <= y2; y++) {
                raster.setPixel(x1, y,color);
            }
            return;
        }

        // y = k * x + q
        double k = (double) (y2 - y1) / (x2 - x1);
        double q = y1 - k * x1;

        if (Math.abs(k) <= 1) {
            // řídicí osa x
            if (x1 > x2) {
                int t = x1; x1 = x2; x2 = t;
            }
            for (int x = x1; x <= x2; x++) {
                int y = (int) Math.round(k * x + q);
                raster.setPixel(x, y,color);
            }
        } else {
            // strmá úsečka, řídicí osa y
            if (y1 > y2) {
                int t = y1; y1 = y2; y2 = t;
            }
            for (int y = y1; y <= y2; y++) {
                int x = (int) Math.round((y - q) / k);
                raster.setPixel(x, y,color);
            }
        }
    }
}

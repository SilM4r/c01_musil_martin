package raster;

import model.Point2D;

/** Plátno, do kterého se kreslí po jednotlivých pixelech. */
public interface Raster {
    void setPixel(int x, int y, int color);

    void setPixel(Point2D point, int color);

    int getWidth();

    int getHeight();

    void clear();
}

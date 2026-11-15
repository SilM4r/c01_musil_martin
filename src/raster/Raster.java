package raster;

public interface Raster {
    void setPixel(int x, int y, int color);

    // mimo plátno vrací -1
    int getPixelColor(int x, int y);

    int getWidth();

    int getHeight();

    void clear();
}

package raster;

import model.Point2D;

import java.awt.image.BufferedImage;

/** Raster nad BufferedImage, který Panel vykresluje na obrazovku. */
public class RasterBufferedImage implements Raster {

    private final BufferedImage image;

    public RasterBufferedImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public void setPixel(int x, int y, int color) {
        // pixely mimo plátno se přeskočí, setRGB by na nich vyhodil výjimku
        if (x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight()) {
            image.setRGB(x, y, color);
        }
    }

    @Override
    public void setPixel(Point2D point, int color) {
        setPixel(point.getX(), point.getY(), color);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public void clear() {
        image.getGraphics().clearRect(0, 0, image.getWidth(), image.getHeight());
    }
}

package raster;

import java.awt.image.BufferedImage;

public class RasterBufferedImage implements Raster {

    private final BufferedImage image;

    public RasterBufferedImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        clear();
    }

    @Override
    public void setPixel(int x, int y, int color) {
        // pixely mimo plátno se přeskočí, setRGB by na nich vyhodil výjimku
        if (x >= 0 && x < this.getWidth() && y >= 0 && y < this.getHeight()) {
            image.setRGB(x, y, color);
        }
    }

    @Override
    public int getPixelColor(int x, int y) {
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            // getRGB vrací i alfa kanál, bez něj jde barva porovnat s 0xRRGGBB
            return image.getRGB(x, y) & 0x00ffffff;
        }
        return -1;
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
        java.awt.Graphics g = image.getGraphics();
        g.setColor(new java.awt.Color(Context.Color.BACKGROUND_COLOR));
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
    }
}

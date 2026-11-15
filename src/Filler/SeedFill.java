package Filler;

import Context.Color;
import model.Point2D;
import raster.Raster;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Semínkový algoritmus (Seed-Fill / 4-směrové záplavové vyplňování).
 * Používá explicitní zásobník (ArrayDeque) místo rekurze,
 * aby nedocházelo k chybě java.lang.StackOverflowError při větších plochách.
 */
public class SeedFill implements Filler {

    private Point2D seed;
    private Raster raster;
    private int color = Color.FILL_COLOR;
    private int backgroundColor = Color.BACKGROUND_COLOR;

    public SeedFill(Raster raster) {
        this.raster = raster;
    }

    @Override
    public void fill() {
        if (seed == null || raster == null) {
            return;
        }

        color = Color.FILL_COLOR;
        backgroundColor = raster.getPixelColor(seed.getX(), seed.getY());
        if (backgroundColor == -1 || backgroundColor == color) {
            return;
        }

        fill(seed);
    }

    public void fill(Point2D point) {
        if (point == null || raster == null) {
            return;
        }

        backgroundColor = raster.getPixelColor(point.getX(), point.getY());
        if (backgroundColor == -1 || backgroundColor == color) {
            return;
        }

        Deque<Point2D> stack = new ArrayDeque<>();
        raster.setPixel(point.getX(), point.getY(), color);
        stack.push(point);

        while (!stack.isEmpty()) {
            Point2D p = stack.pop();
            int x = p.getX();
            int y = p.getY();

            if (x + 1 < raster.getWidth() && raster.getPixelColor(x + 1, y) == backgroundColor) {
                raster.setPixel(x + 1, y, color);
                stack.push(new Point2D(x + 1, y));
            }
            if (x - 1 >= 0 && raster.getPixelColor(x - 1, y) == backgroundColor) {
                raster.setPixel(x - 1, y, color);
                stack.push(new Point2D(x - 1, y));
            }
            if (y + 1 < raster.getHeight() && raster.getPixelColor(x, y + 1) == backgroundColor) {
                raster.setPixel(x, y + 1, color);
                stack.push(new Point2D(x, y + 1));
            }
            if (y - 1 >= 0 && raster.getPixelColor(x, y - 1) == backgroundColor) {
                raster.setPixel(x, y - 1, color);
                stack.push(new Point2D(x, y - 1));
            }
        }
    }

    public void setSeed(Point2D seed) {
        this.seed = seed;
    }

    public Point2D getSeed() {
        return seed;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setRaster(Raster raster) {
        this.raster = raster;
    }
}

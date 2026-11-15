package Filler;

import Context.Color;
import model.Point2D;
import raster.Raster;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 4-směrové záplavové vyplňování. Vlastní zásobník místo rekurze,
 * aby velká plocha neshodila StackOverflowError.
 */
public class SeedFill implements Filler {

    private final Raster raster;
    private Point2D seed;
    private int color = Color.FILL_COLOR;

    public SeedFill(Raster raster) {
        this.raster = raster;
    }

    @Override
    public void fill() {
        if (seed == null) {
            return;
        }

        // vyplňuje se souvislá oblast barvy, na kterou se kliklo
        int backgroundColor = raster.getPixelColor(seed.getX(), seed.getY());
        if (backgroundColor == -1 || backgroundColor == color) {
            return;
        }

        // pixel se obarví hned při vložení, aby se na zásobník nedostal dvakrát
        Deque<Point2D> stack = new ArrayDeque<>();
        raster.setPixel(seed.getX(), seed.getY(), color);
        stack.push(seed);

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

    // bez alfa kanálu (JColorChooser ho přidává), jinak by se nerovnala getPixelColor a fill by se zacyklil
    @Override
    public void setColor(int color) {
        this.color = color & 0x00ffffff;
    }
}

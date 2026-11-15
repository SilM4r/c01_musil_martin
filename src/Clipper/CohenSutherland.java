package Clipper;

import model.Line;
import model.Point2D;
import model.Polygon;

import java.util.Optional;

/** Ořez úseček obdélníkovým oknem rovnoběžným s osami. */
public class CohenSutherland {

    // bity kódu polohy; osa y míří dolů, TOP je proto y < yMin
    public static final int INSIDE = 0;
    public static final int LEFT   = 1;
    public static final int RIGHT  = 2;
    public static final int TOP    = 4;
    public static final int BOTTOM = 8;

    private final double xMin, yMin, xMax, yMax;

    // okno = obálka polygonu, volající ověří, že jde o obdélník
    public CohenSutherland(Polygon clipPolygon) {
        double[] bb = clipPolygon.getBoundingBox();
        this.xMin = bb[0];
        this.yMin = bb[1];
        this.xMax = bb[2];
        this.yMax = bb[3];
    }

    public int computeOutCode(double x, double y) {
        int code = INSIDE;
        if (x < xMin) {
            code |= LEFT;
        } else if (x > xMax) {
            code |= RIGHT;
        }
        if (y < yMin) {
            code |= TOP;
        } else if (y > yMax) {
            code |= BOTTOM;
        }
        return code;
    }

    // Optional.empty(), když úsečka leží celá mimo okno
    public Optional<Line> clip(Line line) {
        if (line == null) {
            return Optional.empty();
        }

        double x1 = line.getStart().getX();
        double y1 = line.getStart().getY();
        double x2 = line.getEnd().getX();
        double y2 = line.getEnd().getY();

        int code1 = computeOutCode(x1, y1);
        int code2 = computeOutCode(x2, y2);

        while (true) {
            // oba konce uvnitř
            if ((code1 | code2) == 0) {
                Point2D p1 = new Point2D((int) Math.round(x1), (int) Math.round(y1));
                Point2D p2 = new Point2D((int) Math.round(x2), (int) Math.round(y2));
                return Optional.of(new Line(p1, p2, line.getColor()));
            }

            // oba konce za stejnou hranou okna
            if ((code1 & code2) != 0) {
                return Optional.empty();
            }

            // vnější konec se posune na průsečík s hranou, za kterou leží.
            // Dělení nulou nehrozí: vodorovná úsečka nad/pod oknem skončí už předchozím testem.
            int outCode = (code1 != 0) ? code1 : code2;
            double x = 0, y = 0;

            if ((outCode & TOP) != 0) {
                y = yMin;
                x = x1 + (x2 - x1) * (yMin - y1) / (y2 - y1);
            } else if ((outCode & BOTTOM) != 0) {
                y = yMax;
                x = x1 + (x2 - x1) * (yMax - y1) / (y2 - y1);
            } else if ((outCode & RIGHT) != 0) {
                x = xMax;
                y = y1 + (y2 - y1) * (xMax - x1) / (x2 - x1);
            } else if ((outCode & LEFT) != 0) {
                x = xMin;
                y = y1 + (y2 - y1) * (xMin - x1) / (x2 - x1);
            }

            if (outCode == code1) {
                x1 = x;
                y1 = y;
                code1 = computeOutCode(x1, y1);
            } else {
                x2 = x;
                y2 = y;
                code2 = computeOutCode(x2, y2);
            }
        }
    }
}

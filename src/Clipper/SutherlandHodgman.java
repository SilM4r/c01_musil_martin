package Clipper;

import model.Point2D;
import model.Polygon;

import java.util.ArrayList;
import java.util.List;

/** Ořez libovolného n-úhelníku konvexním ořezávacím polygonem. */
public class SutherlandHodgman implements Clipper {

    @Override
    public Polygon clip(Polygon inPolygon, Polygon clipPolygon) {
        if (inPolygon == null || inPolygon.getSize() < 3 || clipPolygon == null || clipPolygon.getSize() < 3) {
            return new Polygon();
        }

        // test vnitřní strany v isInsidePineda počítá s ořezávačem proti směru hodinových ručiček
        List<Point2D> clipPoints = clipPolygon.getPointsCCW();

        List<Point2D> in = new ArrayList<>(inPolygon.getPoints());
        List<Point2D> out = new ArrayList<>();

        for (int i = 0; i < clipPoints.size(); i++) {
            Point2D edgeStart = clipPoints.get(i);
            Point2D edgeEnd = clipPoints.get((i + 1) % clipPoints.size());

            out.clear();
            if (in.isEmpty()) {
                break;
            }

            Point2D v1 = in.get(in.size() - 1);
            for (Point2D v2 : in) {
                boolean v2Inside = isInsidePineda(v2, edgeStart, edgeEnd);
                boolean v1Inside = isInsidePineda(v1, edgeStart, edgeEnd);

                if (v2Inside) {
                    if (!v1Inside) {
                        // OUT -> IN: průsečík a v2
                        out.add(intersection(v1, v2, edgeStart, edgeEnd));
                    }
                    // IN -> IN: jen v2
                    out.add(v2);
                } else if (v1Inside) {
                    // IN -> OUT: jen průsečík (OUT -> OUT nepřidá nic)
                    out.add(intersection(v1, v2, edgeStart, edgeEnd));
                }

                v1 = v2;
            }

            // výstup ořezu jednou hranou je vstupem pro další hranu
            in = new ArrayList<>(out);
        }

        Polygon result = new Polygon();
        result.setColor(inPolygon.getColor());
        for (Point2D p : in) {
            result.addPoint(p);
        }
        return result;
    }

    // leží p ve vnitřní polorovině hrany p1 -> p2? (skalární součin s normálou)
    public boolean isInsidePineda(Point2D p, Point2D p1, Point2D p2) {
        long tx = (long) p2.getX() - p1.getX();
        long ty = (long) p2.getY() - p1.getY();

        // osa y míří na obrazovce dolů, proto normála (ty, -tx) a ne (-ty, tx)
        long nx = ty;
        long ny = -tx;

        long vx = (long) p.getX() - p1.getX();
        long vy = (long) p.getY() - p1.getY();

        return vx * nx + vy * ny >= 0;
    }

    // průsečík úsečky v1 -> v2 s přímkou p1 -> p2
    public Point2D intersection(Point2D v1, Point2D v2, Point2D p1, Point2D p2) {
        double x1 = v1.getX(), y1 = v1.getY();
        double x2 = v2.getX(), y2 = v2.getY();
        double x3 = p1.getX(), y3 = p1.getY();
        double x4 = p2.getX(), y4 = p2.getY();

        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (Math.abs(denom) < 1e-9) {
            return v1; // rovnoběžné přímky
        }

        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom;

        int ix = (int) Math.round(x1 + t * (x2 - x1));
        int iy = (int) Math.round(y1 + t * (y2 - y1));

        return new Point2D(ix, iy);
    }
}

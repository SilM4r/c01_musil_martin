package Clipper;

import model.Polygon;

public interface Clipper {

    Polygon clip(Polygon in, Polygon clipPolygon);
}

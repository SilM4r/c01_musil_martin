package controller;

import Context.DrawMode;
import Context.ToolMode;
import model.Line;
import model.Point2D;
import model.Polygon;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerTrivial;
import rasterize.PolygonRasterizer;
import view.Panel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Ovládání:
 *  levé tlačítko  – kreslí úsečku / přidává vrchol polygonu (podle režimu M)
 *  pravé tlačítko – přesouvá / maže nejbližší vrchol (podle nástroje N)
 *  C – smaže scénu, M – přepne úsečka/polygon, N – přepne přesun/mazání
 */
public class Controller2D {
    private static final int GRAB_RANGE = 10;

    private final Panel panel;
    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;

    private final Polygon polygon = new Polygon();
    private final List<Line> lines = new ArrayList<>();

    private DrawMode drawMode = DrawMode.POLYGON;
    private ToolMode toolMode = ToolMode.EDIT;

    // vrchol chycený pravým tlačítkem, -1 = žádný
    private int draggedPointIndex = -1;

    public Controller2D(Panel panel) {
        this.panel = panel;

        lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        polygonRasterizer = new PolygonRasterizer(lineRasterizer);

        initListeners();
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point2D point = new Point2D(e.getX(), e.getY());

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (drawMode == DrawMode.LINE) {
                        lines.add(new Line(point, point));
                    } else {
                        polygon.addPoint(point);
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    int index = polygon.nearestPoint(point, GRAB_RANGE);
                    if (index != -1) {
                        if (toolMode == ToolMode.DELETE) {
                            polygon.remove(index);
                        } else if (drawMode == DrawMode.POLYGON) {
                            // index se hledá jen při stisku, aby bod při rychlém tažení neutekl
                            draggedPointIndex = index;
                        }
                    }
                }

                drawScene();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    draggedPointIndex = -1;
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point2D point = new Point2D(e.getX(), e.getY());

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (drawMode == DrawMode.LINE) {
                        if (lines.isEmpty()) return;
                        Line last = lines.get(lines.size() - 1);
                        lines.set(lines.size() - 1, new Line(last.getStart(), point));
                    } else {
                        if (polygon.getSize() == 0) return;
                        // tažením se posouvá právě přidaný vrchol
                        polygon.setPoint(polygon.getSize() - 1, point);
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    if (draggedPointIndex == -1) return;
                    polygon.setPoint(draggedPointIndex, point);
                }

                drawScene();
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    clearScene();
                } else if (e.getKeyCode() == KeyEvent.VK_M) {
                    if (drawMode == DrawMode.LINE) {
                        drawMode = DrawMode.POLYGON;
                    } else {
                        drawMode = DrawMode.LINE;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_N) {
                    if (toolMode == ToolMode.EDIT) {
                        toolMode = ToolMode.DELETE;
                    } else {
                        toolMode = ToolMode.EDIT;
                    }
                }
            }
        });
    }

    // celá scéna se kreslí znovu od nuly, aby nezůstávaly staré čáry
    private void drawScene() {
        panel.getRaster().clear();

        if (!lines.isEmpty()) {
            for (Line line : lines) {
                lineRasterizer.rasterize(line);
            }
        }
        polygonRasterizer.rasterize(polygon);

        panel.repaint();
    }

    private void clearScene() {
        panel.getRaster().clear();
        polygon.clear();
        lines.clear();
        panel.repaint();
    }
}

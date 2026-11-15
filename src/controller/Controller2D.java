package controller;

import Context.DrawMode;
import Context.ToolMode;
import Filler.ScanLineFill;
import Filler.SeedFill;
import model.Line;
import model.Point2D;
import model.Polygon;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerTrivial;
import rasterize.PolygonRasterizer;
import view.ControlPanel;
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
    private final ControlPanel controlPanel;
    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;
    private final SeedFill seedFill;
    private final ScanLineFill scanLineFill;

    private static class SeedOperation {
        final Point2D point;
        final int color;
        SeedOperation(Point2D point, int color) {
            this.point = point;
            this.color = color;
        }
    }

    private final List<SeedOperation> seedOperations = new ArrayList<>();
    private boolean scanLineActive = false;

    private final Polygon polygon = new Polygon();
    private final List<Line> lines = new ArrayList<>();

    private DrawMode drawMode = DrawMode.POLYGON;
    private ToolMode toolMode = ToolMode.EDIT;

    private int currentLineColor = Context.Color.COLOR;
    private int currentFillColor = Context.Color.FILL_COLOR;

    private boolean shift = false;

    // vrchol chycený pravým tlačítkem, -1 = žádný
    private int draggedPointIndex = -1;

    public Controller2D(Panel panel) {
        this(panel, null);
    }

    public Controller2D(Panel panel, ControlPanel controlPanel) {
        this.panel = panel;
        this.controlPanel = controlPanel;

        lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        polygonRasterizer = new PolygonRasterizer(lineRasterizer);
        seedFill = new SeedFill(panel.getRaster());
        scanLineFill = new ScanLineFill(lineRasterizer, polygonRasterizer);

        initListeners();
        initControlPanel();
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point2D point = new Point2D(e.getX(), e.getY());

                // Vyplňovací nástroje reagují na kliknutí na plátno
                if (toolMode == ToolMode.SEED_FILL) {
                    seedFill.setColor(Context.Color.FILL_COLOR);
                    seedFill.setSeed(point);
                    seedFill.fill();
                    seedOperations.add(new SeedOperation(point, Context.Color.FILL_COLOR));
                    panel.repaint();
                    return;
                }

                if (toolMode == ToolMode.SCAN_LINE) {
                    if (polygon.getSize() >= 3) {
                        scanLineFill.setColor(Context.Color.FILL_COLOR);
                        scanLineFill.setPolygon(polygon);
                        scanLineFill.fill();
                        scanLineActive = true;
                        panel.repaint();
                    }
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (drawMode == DrawMode.LINE) {
                        lines.add(new Line(point, point, currentLineColor));
                    } else {
                        if (polygon.getSize() == 0) {
                            polygon.setColor(currentLineColor);
                        }
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
                        boolean isShiftActive = shift || e.isShiftDown();
                        if (isShiftActive) {
                            int realX = point.getX() - last.getStart().getX();
                            int realY = point.getY() - last.getStart().getY();

                            int absRealX = Math.abs(realX);
                            int absRealY = Math.abs(realY);

                            int i = Math.abs(absRealX - absRealY);
                            if ( i < 50)
                            {
                                int i2 = Math.min(absRealX, absRealY);

                                lines.set(lines.size() - 1, new Line(last.getStart(), new Point2D(last.getStart().getX() + i2 * Integer.signum(realX),last.getStart().getY() + i2*Integer.signum(realY)), last.getColor()));
                            }
                            else if ((Math.abs(point.getX() - last.getStart().getX()) > Math.abs(point.getY() - last.getStart().getY()))) {
                                lines.set(lines.size() - 1, new Line(last.getStart(), new Point2D(point.getX(),last.getStart().getY()), last.getColor()));
                            } else {
                                lines.set(lines.size() - 1, new Line(last.getStart(), new Point2D(last.getStart().getX(), point.getY()), last.getColor()));
                            }
                        }
                        else
                        {
                            lines.set(lines.size() - 1, new Line(last.getStart(), point, last.getColor()));
                        }

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
                    drawMode = (drawMode == DrawMode.LINE) ? DrawMode.POLYGON : DrawMode.LINE;
                    if (controlPanel != null) {
                        controlPanel.setDrawMode(drawMode);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_N) {
                    switch (toolMode) {
                        case EDIT -> toolMode = ToolMode.DELETE;
                        case DELETE -> toolMode = ToolMode.SEED_FILL;
                        case SEED_FILL -> toolMode = ToolMode.SCAN_LINE;
                        case SCAN_LINE -> toolMode = ToolMode.EDIT;
                    }
                    if (controlPanel != null) {
                        controlPanel.setToolMode(toolMode);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shift = !shift;
                    if (controlPanel != null) {
                        controlPanel.setShift(shift);
                    }
                }
            }
        });
    }

    private void initControlPanel() {
        if (controlPanel == null) return;

        controlPanel.setDrawMode(drawMode);
        controlPanel.setToolMode(toolMode);
        controlPanel.setShift(shift);
        controlPanel.setLineColor(new java.awt.Color(currentLineColor));
        controlPanel.setFillColor(new java.awt.Color(currentFillColor));

        controlPanel.addDrawModeListener(mode -> {
            drawMode = mode;
            panel.requestFocusInWindow();
        });

        controlPanel.addToolModeListener(mode -> {
            toolMode = mode;
            panel.requestFocusInWindow();
        });

        controlPanel.addShiftListener(s -> {
            shift = s;
            panel.requestFocusInWindow();
        });

        controlPanel.addLineColorListener(color -> {
            currentLineColor = color.getRGB();
            Context.Color.COLOR = currentLineColor;
            polygon.setColor(currentLineColor);
            drawScene();
            panel.requestFocusInWindow();
        });

        controlPanel.addFillColorListener(color -> {
            currentFillColor = color.getRGB();
            Context.Color.FILL_COLOR = currentFillColor;
            panel.requestFocusInWindow();
        });

        controlPanel.addClearListener(() -> {
            clearScene();
            panel.requestFocusInWindow();
        });
    }

    public int getCurrentLineColor() {
        return currentLineColor;
    }

    public int getCurrentFillColor() {
        return currentFillColor;
    }

    // celá scéna se kreslí znovu od nuly, aby nezůstávaly staré čáry
    private void drawScene() {
        panel.getRaster().clear();

        if (scanLineActive && polygon.getSize() >= 3) {
            scanLineFill.setColor(Context.Color.FILL_COLOR);
            scanLineFill.setPolygon(polygon);
            scanLineFill.fill();
        }

        if (!lines.isEmpty()) {
            for (Line line : lines) {
                lineRasterizer.rasterize(line);
            }
        }
        polygonRasterizer.rasterize(polygon);

        for (SeedOperation op : seedOperations) {
            seedFill.setColor(op.color);
            seedFill.setSeed(op.point);
            seedFill.fill();
        }

        panel.repaint();
    }

    private void clearScene() {
        panel.getRaster().clear();
        polygon.clear();
        lines.clear();
        seedOperations.clear();
        scanLineActive = false;
        panel.repaint();
    }
}

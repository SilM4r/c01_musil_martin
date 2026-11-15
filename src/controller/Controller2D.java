package controller;

import Clipper.CohenSutherland;
import Clipper.SutherlandHodgman;
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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Obsluha myši, kláves a bočního panelu; po každé změně překreslí celou scénu. */
public class Controller2D {
    private static final int GRAB_RANGE = 10;

    private final Panel panel;
    private final ControlPanel controlPanel;
    private final LineRasterizer lineRasterizer;
    private final PolygonRasterizer polygonRasterizer;
    private final SeedFill seedFill;
    private final ScanLineFill scanLineFill;
    private final SutherlandHodgman sutherlandHodgman = new SutherlandHodgman();

    // raster se při každé změně maže, proto se výplně pamatují (seed body, scanLineActive) a při překreslení provedou znovu
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
    private final Polygon clippingPolygon = new Polygon();
    private final List<Line> lines = new ArrayList<>();

    private DrawMode drawMode = DrawMode.POLYGON;
    private ToolMode toolMode = ToolMode.EDIT;

    private int currentLineColor = Context.Color.COLOR;
    private int currentFillColor = Context.Color.FILL_COLOR;
    private int currentClippingColor = Context.Color.CLIPPING_COLOR;

    private boolean shift = false;

    // vrchol chycený pravým tlačítkem, -1 = žádný
    private int draggedPointIndex = -1;
    private Polygon draggedPolygon = null;

    public Controller2D(Panel panel, ControlPanel controlPanel) {
        this.panel = panel;
        this.controlPanel = controlPanel;

        lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        polygonRasterizer = new PolygonRasterizer(lineRasterizer);
        seedFill = new SeedFill(panel.getRaster());
        scanLineFill = new ScanLineFill(lineRasterizer, polygonRasterizer);
        clippingPolygon.setColor(currentClippingColor);

        initListeners();
        initControlPanel();
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point2D point = new Point2D(e.getX(), e.getY());

                // výplně reagují na kliknutí kterýmkoli tlačítkem
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
                    boolean isShiftActive = shift || e.isShiftDown();
                    if (drawMode == DrawMode.LINE) {
                        lines.add(new Line(point, point, currentLineColor));
                    } else if (drawMode == DrawMode.POLYGON) {
                        if (polygon.getSize() == 0) {
                            polygon.setColor(currentLineColor);
                        }
                        Point2D ptToAdd = point;
                        if (isShiftActive && polygon.getSize() >= 1) {
                            ptToAdd = snapPoint(polygon.getPoint(polygon.getSize() - 1), point);
                        }
                        polygon.addPoint(ptToAdd);
                    } else if (drawMode == DrawMode.CLIPPING_POLYGON) {
                        if (clippingPolygon.getSize() == 0) {
                            clippingPolygon.setColor(currentClippingColor);
                        }
                        Point2D ptToAdd = point;
                        if (isShiftActive && clippingPolygon.getSize() >= 1) {
                            ptToAdd = snapPoint(clippingPolygon.getPoint(clippingPolygon.getSize() - 1), point);
                        }
                        clippingPolygon.addPoint(ptToAdd);
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    int indexPoly = polygon.nearestPoint(point, GRAB_RANGE);
                    int indexClip = clippingPolygon.nearestPoint(point, GRAB_RANGE);

                    if (toolMode == ToolMode.DELETE) {
                        if (indexPoly != -1) {
                            polygon.remove(indexPoly);
                        } else if (indexClip != -1) {
                            clippingPolygon.remove(indexClip);
                        }
                    } else if (toolMode == ToolMode.EDIT) {
                        if (indexPoly != -1) {
                            draggedPolygon = polygon;
                            draggedPointIndex = indexPoly;
                        } else if (indexClip != -1) {
                            draggedPolygon = clippingPolygon;
                            draggedPointIndex = indexClip;
                        }
                    }
                }

                drawScene();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    draggedPointIndex = -1;
                    draggedPolygon = null;
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // při vyplňování se nic nekreslí ani needituje (stejně jako v mousePressed)
                if (toolMode == ToolMode.SEED_FILL || toolMode == ToolMode.SCAN_LINE) {
                    return;
                }

                Point2D point = new Point2D(e.getX(), e.getY());

                if (SwingUtilities.isLeftMouseButton(e)) {
                    boolean isShiftActive = shift || e.isShiftDown();
                    if (drawMode == DrawMode.LINE) {
                        if (lines.isEmpty()) return;
                        Line last = lines.get(lines.size() - 1);
                        if (isShiftActive) {
                            Point2D snapped = snapPoint(last.getStart(), point);
                            lines.set(lines.size() - 1, new Line(last.getStart(), snapped, last.getColor()));
                        } else {
                            lines.set(lines.size() - 1, new Line(last.getStart(), point, last.getColor()));
                        }
                    } else if (drawMode == DrawMode.POLYGON) {
                        if (polygon.getSize() == 0) return;
                        if (isShiftActive && polygon.getSize() >= 2) {
                            Point2D prev = polygon.getPoint(polygon.getSize() - 2);
                            polygon.setPoint(polygon.getSize() - 1, snapPoint(prev, point));
                        } else {
                            polygon.setPoint(polygon.getSize() - 1, point);
                        }
                    } else if (drawMode == DrawMode.CLIPPING_POLYGON) {
                        if (clippingPolygon.getSize() == 0) return;
                        if (isShiftActive && clippingPolygon.getSize() >= 2) {
                            Point2D prev = clippingPolygon.getPoint(clippingPolygon.getSize() - 2);
                            clippingPolygon.setPoint(clippingPolygon.getSize() - 1, snapPoint(prev, point));
                        } else {
                            clippingPolygon.setPoint(clippingPolygon.getSize() - 1, point);
                        }
                    }
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    if (draggedPolygon == null || draggedPointIndex == -1) return;
                    draggedPolygon.setPoint(draggedPointIndex, point);
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
                    switch (drawMode) {
                        case POLYGON -> drawMode = DrawMode.CLIPPING_POLYGON;
                        case CLIPPING_POLYGON -> drawMode = DrawMode.LINE;
                        case LINE -> drawMode = DrawMode.POLYGON;
                    }
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
                } else if (e.getKeyCode() == KeyEvent.VK_V) {
                    clipPolygon();
                } else if (e.getKeyCode() == KeyEvent.VK_B) {
                    clipLines();
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
        controlPanel.setClippingColor(new java.awt.Color(currentClippingColor));

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

        controlPanel.addClippingColorListener(color -> {
            currentClippingColor = color.getRGB();
            Context.Color.CLIPPING_COLOR = currentClippingColor;
            clippingPolygon.setColor(currentClippingColor);
            drawScene();
            panel.requestFocusInWindow();
        });

        controlPanel.addClearListener(() -> {
            clearScene();
            panel.requestFocusInWindow();
        });

        controlPanel.addClippingListener(() -> {
            clipPolygon();
            panel.requestFocusInWindow();
        });

        controlPanel.addClipLinesListener(() -> {
            clipLines();
            panel.requestFocusInWindow();
        });
    }

    private void clipPolygon() {
        if (polygon.getSize() >= 3 && clippingPolygon.getSize() >= 3) {
            Polygon clipped = sutherlandHodgman.clip(polygon, clippingPolygon);
            polygon.clear();
            for (Point2D p : clipped.getPoints()) {
                polygon.addPoint(p);
            }
            drawScene();
        }
    }

    private void clipLines() {
        if (lines.isEmpty()) {
            JOptionPane.showMessageDialog(panel,
                "Na scéně nejsou žádné úsečky k ořezání.",
                "Informace",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (clippingPolygon.getSize() != 4 || !clippingPolygon.isAxisAlignedRectangle()) {
            JOptionPane.showMessageDialog(panel,
                "Ořezávací polygon musí mít přesně 4 vrcholy a tvořit pravoúhlý obdélník (či čtverec) rovnoběžný s osami X a Y!\n\n" +
                "Tip: Přepněte do režimu 'Polygon - Ořezávač', zapněte [Shift] a klikněte 4 pravoúhlé vrcholy.",
                "Neplatné ořezávací okno pro Cohen-Sutherland",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        CohenSutherland cs = new CohenSutherland(clippingPolygon);
        List<Line> clippedLines = new ArrayList<>();
        for (Line line : lines) {
            Optional<Line> clipped = cs.clip(line);
            clipped.ifPresent(clippedLines::add);
        }

        lines.clear();
        lines.addAll(clippedLines);
        drawScene();
    }

    // zarovná bod na 0°, 45° nebo 90° vůči start; když se |dx| a |dy| liší o méně než 50 px, bere se úhlopříčka
    private Point2D snapPoint(Point2D start, Point2D current) {
        int realX = current.getX() - start.getX();
        int realY = current.getY() - start.getY();

        int absRealX = Math.abs(realX);
        int absRealY = Math.abs(realY);

        int diff = Math.abs(absRealX - absRealY);
        if (diff < 50) {
            int diag = Math.min(absRealX, absRealY);
            return new Point2D(start.getX() + diag * Integer.signum(realX), start.getY() + diag * Integer.signum(realY));
        } else if (absRealX > absRealY) {
            return new Point2D(current.getX(), start.getY());
        } else {
            return new Point2D(start.getX(), current.getY());
        }
    }

    // celá scéna se kreslí znovu od nuly, aby nezůstávaly staré čáry;
    // seed fill je až na konci, protože vyplňuje oblasti ohraničené už nakreslenými čarami
    private void drawScene() {
        panel.getRaster().clear();

        if (scanLineActive && polygon.getSize() >= 3) {
            scanLineFill.setColor(Context.Color.FILL_COLOR);
            scanLineFill.setPolygon(polygon);
            scanLineFill.fill();
        }

        for (Line line : lines) {
            lineRasterizer.rasterize(line);
        }
        polygonRasterizer.rasterize(polygon);

        if (clippingPolygon.getSize() > 0) {
            polygonRasterizer.rasterize(clippingPolygon);
        }

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
        clippingPolygon.clear();
        lines.clear();
        seedOperations.clear();
        scanLineActive = false;
        panel.repaint();
    }
}

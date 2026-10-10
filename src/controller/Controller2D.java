package controller;

import Context.DrawMode;
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

public class Controller2D {
    private final Panel panel;
    private LineRasterizer lineRasterizer;
    private PolygonRasterizer polygonRasterizer;

    private final Polygon polygon;

    private final ArrayList<Line> lines = new ArrayList<Line>();

    private DrawMode draweMode = DrawMode.POLYGON;



    public Controller2D(Panel panel) {
        this.panel = panel;

        lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        polygonRasterizer = new PolygonRasterizer(lineRasterizer);

        polygon = new Polygon();

        initListeners();
    }

    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1) {
                    Point2D point = new Point2D(e.getX(), e.getY());
                    if (draweMode == DrawMode.LINE){
                        lines.add(new Line(point,point));
                    }
                    else {
                        polygon.addPoint(point);
                    }
                }
                else if(e.getButton() == MouseEvent.BUTTON3) {

                }

                drawScene();
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON1) {
                    if (draweMode == DrawMode.LINE){
                        if (lines.isEmpty()){
                            return;
                        }

                        Line line = lines.get(lines.size()-1);
                        lines.set(lines.size()-1,new Line(line.getStart(),new Point2D(e.getX(),e.getY())));

                    }
                    else {
                        if (polygon.getSize() == 0){
                            return;
                        }
                        polygon.setPoint(polygon.getSize() -1,new Point2D(e.getX(),e.getY()));
                    }

                }
                else if(e.getButton() == MouseEvent.BUTTON3) {


                }

                drawScene();
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_C){
                    clearScene();
                } else if (e.getKeyCode() == KeyEvent.VK_M) {
                    if (draweMode == DrawMode.LINE){
                        draweMode = DrawMode.POLYGON;
                    }
                    else {
                        draweMode = DrawMode.LINE;
                    }
                }

            }
        });


    }

    private void drawScene() {
        panel.getRaster().clear();

        if (!lines.isEmpty()){
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

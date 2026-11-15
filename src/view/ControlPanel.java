package view;

import Context.DrawMode;
import Context.ToolMode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Boční panel; změny posílá controlleru přes listenery, controller do něj zpětně promítá stav kláves. */
public class ControlPanel extends JPanel {

    private final JRadioButton radioPolygon;
    private final JRadioButton radioClippingPolygon;
    private final JRadioButton radioLine;

    private final JRadioButton radioEdit;
    private final JRadioButton radioDelete;
    private final JRadioButton radioSeedFill;
    private final JRadioButton radioScanLine;

    private final JCheckBox checkShift;
    private final JButton btnClear;
    private final JButton btnClipping;
    private final JButton btnClipLines;

    private Color lineColor = new Color(Context.Color.COLOR);
    private Color fillColor = new Color(Context.Color.FILL_COLOR);
    private Color clippingColor = new Color(Context.Color.CLIPPING_COLOR);

    private final JButton btnLineColor;
    private final JButton btnFillColor;
    private final JButton btnClippingColor;
    private final ColorIcon lineIcon;
    private final ColorIcon fillIcon;
    private final ColorIcon clippingIcon;

    private final List<Consumer<Color>> lineColorListeners = new ArrayList<>();
    private final List<Consumer<Color>> fillColorListeners = new ArrayList<>();
    private final List<Consumer<Color>> clippingColorListeners = new ArrayList<>();

    public ControlPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setPreferredSize(new Dimension(220, 650));

        // režim kreslení
        JPanel drawPanel = new JPanel();
        drawPanel.setLayout(new BoxLayout(drawPanel, BoxLayout.Y_AXIS));
        drawPanel.setBorder(new TitledBorder("Režim kreslení [M]"));
        drawPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        radioPolygon = new JRadioButton("Polygon", true);
        radioClippingPolygon = new JRadioButton("Polygon - Ořezávač");
        radioLine = new JRadioButton("Úsečka");
        radioPolygon.setFocusable(false);
        radioClippingPolygon.setFocusable(false);
        radioLine.setFocusable(false);

        ButtonGroup drawGroup = new ButtonGroup();
        drawGroup.add(radioPolygon);
        drawGroup.add(radioClippingPolygon);
        drawGroup.add(radioLine);

        drawPanel.add(radioPolygon);
        drawPanel.add(Box.createVerticalStrut(4));
        drawPanel.add(radioClippingPolygon);
        drawPanel.add(Box.createVerticalStrut(4));
        drawPanel.add(radioLine);

        // barvy
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.Y_AXIS));
        colorPanel.setBorder(new TitledBorder("Barvy (Color Picker)"));
        colorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lineIcon = new ColorIcon(lineColor, 18);
        btnLineColor = new JButton(" Obrys: " + formatHex(lineColor), lineIcon);
        btnLineColor.setToolTipText("Kliknutím vyberete barvu pro kreslení úseček a obrysu polygonu");
        btnLineColor.setFocusable(false);
        btnLineColor.setHorizontalAlignment(SwingConstants.LEFT);
        btnLineColor.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnLineColor.getPreferredSize().height));
        btnLineColor.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Vyberte barvu obrysu / úsečky", lineColor);
            if (chosen != null) {
                setLineColor(chosen);
                for (Consumer<Color> listener : lineColorListeners) {
                    listener.accept(chosen);
                }
            }
        });

        fillIcon = new ColorIcon(fillColor, 18);
        btnFillColor = new JButton(" Výplň: " + formatHex(fillColor), fillIcon);
        btnFillColor.setToolTipText("Kliknutím vyberete barvu pro vyplňování polygonu");
        btnFillColor.setFocusable(false);
        btnFillColor.setHorizontalAlignment(SwingConstants.LEFT);
        btnFillColor.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnFillColor.getPreferredSize().height));
        btnFillColor.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Vyberte barvu výplně", fillColor);
            if (chosen != null) {
                setFillColor(chosen);
                for (Consumer<Color> listener : fillColorListeners) {
                    listener.accept(chosen);
                }
            }
        });

        clippingIcon = new ColorIcon(clippingColor, 18);
        btnClippingColor = new JButton(" Ořezávač: " + formatHex(clippingColor), clippingIcon);
        btnClippingColor.setToolTipText("Kliknutím vyberete barvu pro ořezávací polygon (clippingPolygon)");
        btnClippingColor.setFocusable(false);
        btnClippingColor.setHorizontalAlignment(SwingConstants.LEFT);
        btnClippingColor.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnClippingColor.getPreferredSize().height));
        btnClippingColor.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Vyberte barvu ořezávacího polygonu", clippingColor);
            if (chosen != null) {
                setClippingColor(chosen);
                for (Consumer<Color> listener : clippingColorListeners) {
                    listener.accept(chosen);
                }
            }
        });

        colorPanel.add(btnLineColor);
        colorPanel.add(Box.createVerticalStrut(6));
        colorPanel.add(btnFillColor);
        colorPanel.add(Box.createVerticalStrut(6));
        colorPanel.add(btnClippingColor);

        // nástroje
        JPanel toolPanel = new JPanel();
        toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.Y_AXIS));
        toolPanel.setBorder(new TitledBorder("Nástroje / Režim [N]"));
        toolPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        radioEdit = new JRadioButton("Přesunout (EDIT)", true);
        radioDelete = new JRadioButton("Smazat (DELETE)");
        radioSeedFill = new JRadioButton("Vyplnit (SeedFill)");
        radioScanLine = new JRadioButton("Vyplnit (ScanLine)");

        radioEdit.setFocusable(false);
        radioDelete.setFocusable(false);
        radioSeedFill.setFocusable(false);
        radioScanLine.setFocusable(false);

        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(radioEdit);
        toolGroup.add(radioDelete);
        toolGroup.add(radioSeedFill);
        toolGroup.add(radioScanLine);

        toolPanel.add(radioEdit);
        toolPanel.add(Box.createVerticalStrut(4));
        toolPanel.add(radioDelete);
        toolPanel.add(Box.createVerticalStrut(4));
        toolPanel.add(radioSeedFill);
        toolPanel.add(Box.createVerticalStrut(4));
        toolPanel.add(radioScanLine);

        // zarovnání
        JPanel modPanel = new JPanel();
        modPanel.setLayout(new BoxLayout(modPanel, BoxLayout.Y_AXIS));
        modPanel.setBorder(new TitledBorder("Zarovnání [Shift]"));
        modPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        checkShift = new JCheckBox("Zarovnat (0°/45°/90°)");
        checkShift.setToolTipText("Zarovná úsečku vodorovně, svisle nebo po 45 stupních");
        checkShift.setFocusable(false);
        modPanel.add(checkShift);

        // akce
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(new TitledBorder("Akce"));
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnClear = new JButton("Vymazat scénu [C]");
        btnClear.setFocusable(false);
        btnClear.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnClear.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnClear.getPreferredSize().height));
        actionPanel.add(btnClear);

        btnClipping = new JButton("Ořez polygonu [V]");
        btnClipping.setToolTipText("Sutherland-Hodgman ořezání polygonu ořezávacím polygonem");
        btnClipping.setFocusable(false);
        btnClipping.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnClipping.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnClipping.getPreferredSize().height));
        actionPanel.add(Box.createVerticalStrut(4));
        actionPanel.add(btnClipping);

        btnClipLines = new JButton("Ořez úseček [B]");
        btnClipLines.setToolTipText("Cohen-Sutherland ořezání úseček obdélníkovým oknem (clippingPolygon)");
        btnClipLines.setFocusable(false);
        btnClipLines.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnClipLines.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnClipLines.getPreferredSize().height));
        actionPanel.add(Box.createVerticalStrut(4));
        actionPanel.add(btnClipLines);

        // BoxLayout jinak panely neroztáhne na šířku sidebaru
        drawPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, drawPanel.getPreferredSize().height));
        colorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, colorPanel.getPreferredSize().height));
        toolPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, toolPanel.getPreferredSize().height));
        modPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, modPanel.getPreferredSize().height));

        add(drawPanel);
        add(Box.createVerticalStrut(8));
        add(colorPanel);
        add(Box.createVerticalStrut(8));
        add(toolPanel);
        add(Box.createVerticalStrut(8));
        add(modPanel);
        add(Box.createVerticalStrut(8));
        add(actionPanel);
        add(Box.createVerticalStrut(8));
        add(Box.createVerticalGlue());
    }

    private String formatHex(Color c) {
        return String.format("#%06X", (0xFFFFFF & c.getRGB()));
    }

    public void setLineColor(Color color) {
        this.lineColor = color;
        Context.Color.COLOR = color.getRGB();
        lineIcon.setColor(color);
        btnLineColor.setText(" Obrys: " + formatHex(color));
        btnLineColor.repaint();
    }

    public void setFillColor(Color color) {
        this.fillColor = color;
        Context.Color.FILL_COLOR = color.getRGB();
        fillIcon.setColor(color);
        btnFillColor.setText(" Výplň: " + formatHex(color));
        btnFillColor.repaint();
    }

    public void setClippingColor(Color color) {
        this.clippingColor = color;
        Context.Color.CLIPPING_COLOR = color.getRGB();
        clippingIcon.setColor(color);
        btnClippingColor.setText(" Ořezávač: " + formatHex(color));
        btnClippingColor.repaint();
    }

    public void addLineColorListener(Consumer<Color> listener) {
        lineColorListeners.add(listener);
    }

    public void addFillColorListener(Consumer<Color> listener) {
        fillColorListeners.add(listener);
    }

    public void addClippingColorListener(Consumer<Color> listener) {
        clippingColorListeners.add(listener);
    }

    public void setDrawMode(DrawMode mode) {
        if (mode == DrawMode.LINE) {
            radioLine.setSelected(true);
        } else if (mode == DrawMode.POLYGON) {
            radioPolygon.setSelected(true);
        } else {
            radioClippingPolygon.setSelected(true);
        }
    }

    public void setToolMode(ToolMode mode) {
        if (mode == ToolMode.EDIT) {
            radioEdit.setSelected(true);
        } else if (mode == ToolMode.DELETE) {
            radioDelete.setSelected(true);
        } else if (mode == ToolMode.SEED_FILL) {
            radioSeedFill.setSelected(true);
        } else if (mode == ToolMode.SCAN_LINE) {
            radioScanLine.setSelected(true);
        }
    }

    public void setShift(boolean shift) {
        checkShift.setSelected(shift);
    }

    public void addDrawModeListener(Consumer<DrawMode> listener) {
        radioPolygon.addActionListener(e -> listener.accept(DrawMode.POLYGON));
        radioClippingPolygon.addActionListener(e -> listener.accept(DrawMode.CLIPPING_POLYGON));
        radioLine.addActionListener(e -> listener.accept(DrawMode.LINE));
    }

    public void addToolModeListener(Consumer<ToolMode> listener) {
        radioEdit.addActionListener(e -> listener.accept(ToolMode.EDIT));
        radioDelete.addActionListener(e -> listener.accept(ToolMode.DELETE));
        radioSeedFill.addActionListener(e -> listener.accept(ToolMode.SEED_FILL));
        radioScanLine.addActionListener(e -> listener.accept(ToolMode.SCAN_LINE));
    }

    public void addShiftListener(Consumer<Boolean> listener) {
        checkShift.addActionListener(e -> listener.accept(checkShift.isSelected()));
    }

    public void addClearListener(Runnable listener) {
        btnClear.addActionListener(e -> listener.run());
    }

    public void addClippingListener(Runnable listener) {
        btnClipping.addActionListener(e -> listener.run());
    }

    public void addClipLinesListener(Runnable listener) {
        btnClipLines.addActionListener(e -> listener.run());
    }

    // čtvereček s vybranou barvou na tlačítku
    private static class ColorIcon implements Icon {
        private Color color;
        private final int size;

        public ColorIcon(Color color, int size) {
            this.color = color;
            this.size = size;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(color);
            g2.fillRect(x, y, size, size);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRect(x, y, size - 1, size - 1);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}

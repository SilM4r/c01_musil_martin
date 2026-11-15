package view;

import javax.swing.*;
import java.awt.*;

/** Hlavní okno aplikace s kreslicím panelem a ovládacím panelem. */
public class Window extends JFrame {

    private final Panel panel;
    private final ControlPanel controlPanel;

    public Window(int width, int height) {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("PGRF1 2024/2025");
        setLayout(new BorderLayout());

        panel = new Panel(width, height);
        controlPanel = new ControlPanel();

        add(panel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        panel.setFocusable(true);
        panel.requestFocusInWindow();
    }

    public Panel getPanel() {
        return panel;
    }

    public ControlPanel getControlPanel() {
        return controlPanel;
    }
}

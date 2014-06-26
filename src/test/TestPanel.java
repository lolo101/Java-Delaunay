package test;

import hoten.voronoi.nodename.as3delaunay.Voronoi;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ItemListener;
import java.util.Random;
import javax.swing.JPanel;

public class TestPanel extends JPanel {

    private final TestGraphImpl graph;
    private final Checkbox cbBiomes = new Checkbox("Biomes");
    private final Checkbox cbRivers = new Checkbox("Rivers");
    private final Checkbox cbSites = new Checkbox("Sites");
    private final Checkbox cbCorners = new Checkbox("Corners");
    private final Checkbox cbDelaunay = new Checkbox("Delaunay");
    private final Checkbox cbVoronoi = new Checkbox("Voronoi");

    private final JPanel mainPanel = new JPanel() {

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            graph.paint(g2d,
                    cbBiomes.getState(),
                    cbRivers.getState(),
                    cbSites.getState(),
                    cbCorners.getState(),
                    cbDelaunay.getState(),
                    cbVoronoi.getState());
        }
    };

    private final ItemListener repainter = e -> mainPanel.repaint();

    TestPanel(Voronoi v) {
        //assemble the voronoi strucutre into a usable graph object representing a map
        this.graph = new TestGraphImpl(v, 2, new Random());

        setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel();
        add(buttonPanel, BorderLayout.NORTH);

        // Controls
        buttonPanel.add(cbBiomes);
        buttonPanel.add(cbRivers);
        buttonPanel.add(cbSites);
        buttonPanel.add(cbCorners);
        buttonPanel.add(cbDelaunay);
        buttonPanel.add(cbVoronoi);
        
        // Main
        mainPanel.setPreferredSize(new Dimension((int) graph.bounds.width, (int) graph.bounds.height));
        mainPanel.addMouseListener(new TestMouseListener(v));
        add(mainPanel);
        
        cbBiomes.addItemListener(repainter);
        cbRivers.addItemListener(repainter);
        cbSites.addItemListener(repainter);
        cbCorners.addItemListener(repainter);
        cbDelaunay.addItemListener(repainter);
        cbVoronoi.addItemListener(repainter);
    }

}

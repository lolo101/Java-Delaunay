package test;

import hoten.voronoi.VoronoiGraph;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

public class TestPanel extends JPanel {

    private final Checkbox cbBiomes = new Checkbox("Biomes", true);
    private final Checkbox cbRivers = new Checkbox("Rivers", true);
    private final Checkbox cbSites = new Checkbox("Sites");
    private final Checkbox cbCorners = new Checkbox("Corners");
    private final Checkbox cbDelaunay = new Checkbox("Delaunay");
    private final Checkbox cbVoronoi = new Checkbox("Voronoi");

    private final VoronoiGraphPanel mainPanel;

    TestPanel(VoronoiGraph graph) {
        mainPanel = new VoronoiGraphPanel(graph);

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
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point point = e.getPoint();
                mainPanel.selectRegionAt(point);
                mainPanel.repaint();
            }
        });
        add(mainPanel);
        mainPanel.setDrawBiomes(cbBiomes.getState());
        mainPanel.setDrawRivers(cbRivers.getState());
        mainPanel.setDrawSites(cbSites.getState());
        mainPanel.setDrawCorners(cbCorners.getState());
        mainPanel.setDrawDelaunay(cbDelaunay.getState());
        mainPanel.setDrawVoronoi(cbVoronoi.getState());
        
        cbBiomes.addItemListener(e -> {
            mainPanel.setDrawBiomes(cbBiomes.getState());
            mainPanel.repaint();
        });
        cbRivers.addItemListener(e -> {
            mainPanel.setDrawRivers(cbRivers.getState());
            mainPanel.repaint();
        });
        cbSites.addItemListener(e -> {
            mainPanel.setDrawSites(cbSites.getState());
            mainPanel.repaint();
        });
        cbCorners.addItemListener(e -> {
            mainPanel.setDrawCorners(cbCorners.getState());
            mainPanel.repaint();
        });
        cbDelaunay.addItemListener(e -> {
            mainPanel.setDrawDelaunay(cbDelaunay.getState());
            mainPanel.repaint();
        });
        cbVoronoi.addItemListener(e -> {
            mainPanel.setDrawVoronoi(cbVoronoi.getState());
            mainPanel.repaint();
        });
    }

}

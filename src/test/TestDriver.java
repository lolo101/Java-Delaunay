package test;

import hoten.voronoi.VoronoiGraph;
import hoten.voronoi.nodename.as3delaunay.Voronoi;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * TestDriver.java
 *
 * @author Connor
 */
public class TestDriver {

    public static void main(String[] args) {
        final int width = 600;
        final int height = 600;
        final int numSites = 3000;
        final long seed = System.nanoTime();
        final Random r = new Random(seed);
        System.out.println("seed: " + seed);

        //make the intial underlying voronoi structure
        final Voronoi v = new Voronoi(numSites, width, height, r);
        //assemble the voronoi strucutre into a usable graph object representing a map
        VoronoiGraph graph = new TestGraphImpl(v, 2, new Random());

        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame("java fortune");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.add(new TestPanel(graph));
            frame.pack();
            frame.setVisible(true);
        });
    }
}

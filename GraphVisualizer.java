import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.ext.JGraphXAdapter;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.model.mxCell;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class GraphVisualizer {
    private SimpleWeightedGraph<String, DefaultWeightedEdge> graph;

    public GraphVisualizer() {
        graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
    }

    // Add a node to the graph
    public void addNode(String nodeName) {
        graph.addVertex(nodeName);
    }

    // Add an edge between two nodes
    public void addEdge(String from, String to, double weight) {
        DefaultWeightedEdge edge = graph.addEdge(from, to);
        if (edge != null) {
            graph.setEdgeWeight(edge, weight);
        }
    }

    // Display the graph in a Swing window
    public void visualize() {
        JGraphXAdapter<String, DefaultWeightedEdge> graphAdapter = new JGraphXAdapter<>(graph);

        // Customize edge labels to show weights
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            double weight = graph.getEdgeWeight(edge);
            mxCell cell = (mxCell) graphAdapter.getEdgeToCellMap().get(edge);
            cell.setValue(String.format("%.2f km", weight)); // Set weight as edge label
        }

        // Apply a circular layout to position nodes
        mxCircleLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        // Create a Swing window for visualization
        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        JFrame frame = new JFrame("Graph Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(graphComponent, BorderLayout.CENTER);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    // Add doctors and patient to the graph
    public static void createGraph(List<Doctor> doctors, double patientLatitude, double patientLongitude) {
        GraphVisualizer visualizer = new GraphVisualizer();

        // Add the patient node
        visualizer.addNode("Patient");

        // Add doctor nodes and edges
        for (Doctor doctor : doctors) {
            visualizer.addNode(doctor.name);
            double distance = DistanceCalculator.calculateDistance(
                patientLatitude, patientLongitude,
                doctor.latitude, doctor.longitude
            );
            visualizer.addEdge("Patient", doctor.name, distance);
        }

        // Add edges between doctors
        for (int i = 0; i < doctors.size(); i++) {
            for (int j = i + 1; j < doctors.size(); j++) {
                double distance = DistanceCalculator.calculateDistance(
                    doctors.get(i).latitude, doctors.get(i).longitude,
                    doctors.get(j).latitude, doctors.get(j).longitude
                );
                visualizer.addEdge(doctors.get(i).name, doctors.get(j).name, distance);
            }
        }

        // Visualize the graph
        visualizer.visualize();
    }
}

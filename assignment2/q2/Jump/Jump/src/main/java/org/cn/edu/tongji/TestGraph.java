package org.cn.edu.tongji;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;


public class TestGraph {
    //the methode that will zoom the graph
    public static void zoomGraphMouseWheelMoved(MouseWheelEvent mwe, ViewPanel view_panel){
        if (Event.ALT_MASK != 0) {
            if (mwe.getWheelRotation() > 0) {
                double new_view_percent = view_panel.getCamera().getViewPercent() + 0.05;
                view_panel.getCamera().setViewPercent(new_view_percent);
            } else if (mwe.getWheelRotation() < 0) {
                double current_view_percent = view_panel.getCamera().getViewPercent();
                if(current_view_percent > 0.05){
                    view_panel.getCamera().setViewPercent(current_view_percent - 0.05);
                }
            }
        }
    }
    public static void main(String[] args) {

        JFrame frame = new JFrame("THIS is a TITLE");
        frame.setLayout(new GridLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 700, 500);
        frame.setPreferredSize(new Dimension(700, 500));


        //Components
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());
        frame.add(panel);

        //create a simple graph
        Graph graph = new SingleGraph("tuto_zoom", false, true);
        graph.addNode("node_1");
        graph.addNode("node_2");
        graph.addEdge("edge_1_2", "node_1","node_2");


        //show the graph in the panel
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        ViewPanel view_panel = viewer.addDefaultView(false);
        Rectangle rec = panel.getBounds();
        view_panel.setBounds(0, 0, rec.width, rec.height);
        view_panel.setPreferredSize(new Dimension(rec.width, rec.height));
        panel.add(view_panel);


        //add a mouse wheel listener to the ViewPanel for zooming the graph
        view_panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {
                TestGraph.zoomGraphMouseWheelMoved(mwe, view_panel);
            }
        });


        frame.setVisible(true);
        graph.addAttribute("ui.stylesheet", "node {size:12px;fill-color:#ff0000;}");
        graph.addAttribute("ui.stylesheet", "edge { shape:angle ;fill-color:#222;}");
    }
    public static void main2(String[] args) {
        JFrame frame = new JFrame("THIS IS A TITLE");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        @SuppressWarnings("serial")
        JPanel panel = new JPanel(new GridLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(640, 480);
            }
        };
        // 设置边框
        panel.setBorder(BorderFactory.createLineBorder(Color.blue, 5));

        Graph graph = new SingleGraph("aaaa", false, true);
        graph.addNode("a");
        graph.addNode("2");
        graph.addNode("5");
        graph.addNode("9");

        graph.setAutoCreate(true);
        graph.setStrict(false);
        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        ViewPanel viewPanel = viewer.addDefaultView(false);
        viewer.enableAutoLayout();
        panel.add(viewPanel);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        graph.addAttribute("ui.stylesheet", "node {size:12px;fill-color:#ff0000;}");
        graph.addAttribute("ui.stylesheet", "edge { shape:angle ;fill-color:#222;}");
    }
}


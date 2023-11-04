package org.cn.edu.tongji;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;


import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class GraphViewer {
    public static void showGraph(Graph graph) {
        JFrame frame = new JFrame("Graph Viewer");
        frame.setLayout(new GridLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setBounds(0, 0, 700, 500);
        frame.setPreferredSize(new Dimension(700, 500));

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());
        frame.add(panel);

        Viewer viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        ViewPanel viewPanel = viewer.addDefaultView(false);
        Rectangle rec = panel.getBounds();
        viewPanel.setBounds(0, 0, rec.width, rec.height);
        viewPanel.setPreferredSize(new Dimension(rec.width, rec.height));
        panel.add(viewPanel);

        viewPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent mwe) {
                zoomGraphMouseWheelMoved(mwe, viewPanel);
            }
        });

        frame.setVisible(true);
    }

    private static void zoomGraphMouseWheelMoved(MouseWheelEvent mwe, ViewPanel viewPanel) {
        int rotation = mwe.getWheelRotation();
        double scaleFactor = 1.1;
        double zoomFactor = Math.pow(scaleFactor, rotation);
        viewPanel.getCamera().setViewPercent(viewPanel.getCamera().getViewPercent() * zoomFactor);
    }

    public static void main(String[] args) {
        // 创建一个简单的图形
        Graph graph = new SingleGraph("tuto_zoom", false, true);
        graph.addNode("node_1");
        graph.addNode("node_2");
        graph.addEdge("edge_1_2", "node_1", "node_2");

        // 调用封装的函数进行图形显示
        showGraph(graph);
    }
}
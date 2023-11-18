package org.cn.edu.tongji;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Main {
    private static final int MAX_HOPS = 3;
    private static final int MAX_EXTERNAL_URLS = 6;
    private static final int CONNECTION_TIMEOUT_MS = 5000;

    public static void main(String[] args) {
        String[] seedUrls = {"https://www.tongji.edu.cn", "https://www.pku.edu.cn", "http://www.sina.com.cn", "https://www.mit.edu"};
//        String[] seedUrls = {"https://www.tongji.edu.cn" }; // for test

        Graph graph = new SingleGraph("WebCrawler");
        Set<String> visitedUrls = new HashSet<>();

        // 开始计时
        long startTime = System.currentTimeMillis();

        // 进入递归
        for (String seedUrl: seedUrls) {
            Node node = graph.addNode(seedUrl);
            node.addAttribute("ui.label", node.getId());
            visitedUrls.add(seedUrl);
            jumpUrl(seedUrl, graph, visitedUrls, 0);
        }

        // 结束计时
        long endTime = System.currentTimeMillis();

        // 打印所需信息
        printGraphStats(graph);
        System.out.println("花费时间：" + (endTime - startTime) + "ms");
        // 画图
        showGraph(graph);
    }

    public static void showGraph(Graph graph) {
        JFrame frame = new JFrame("Graph Viewer");
        frame.setLayout(new GridLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth() / 2;
        int screenHeight = (int) screenSize.getHeight() / 2;
        frame.setBounds(0, 0, screenWidth, screenHeight);
        frame.setPreferredSize(new Dimension(screenWidth, screenHeight));

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

    // 打印图信息
    private static void printGraphStats(Graph graph) {
        int nodeCount = graph.getNodeCount();
        int edgeCount = graph.getEdgeCount();

        Node maxInDegreeNode = null;
        int maxInDegree = -1;
        // 找出入度最大的节点
        for (Node node: graph) {
            int inDegree = node.getInDegree();

            if (inDegree > maxInDegree) {
                maxInDegree = inDegree;
                maxInDegreeNode = node;
            }
        }

        System.out.println("图结点数量：" + nodeCount);
        System.out.println("图边数量：" + edgeCount);
        System.out.println("入度最大的结点URL：" + maxInDegreeNode + "（入度：" + maxInDegree + "）");
    }

    public static boolean sameURL(String url1, String url2) {
        try {
            URL parsedURL1 = new URL(url1);
            URL parsedURL2 = new URL(url2);

            String host1 = parsedURL1.getHost(); // 提取域名部分
            String[] host1Parts = host1.split("\\.");

            String host2 = parsedURL2.getHost();
            String[] host2Parts = host2.split("\\.");
            // 不够三个的算作不相同
            if (host1Parts.length < 3 || host2Parts.length < 3) {
                return false;
            }
            // 检查后三个子域名
            for (int i = 1; i <= 3; ++i) {
                if (!host1Parts[host1Parts.length - i].equals(host2Parts[host2Parts.length - i])) {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            // 处理 URL 解析错误的异常情况
            e.printStackTrace();
            return false;
        }
    }

    private static void jumpUrl(String url, Graph graph, Set<String> visitedUrls, int hops) {
        if (hops > MAX_HOPS) {
            return;
        }
        try {
            Document document = Jsoup.connect(url)
                    .timeout(CONNECTION_TIMEOUT_MS)
                    .get();
            Elements links = document.select("a[href]");

            int externalUrlsCount = 0;
            for (Element link: links) {
                String linkUrl = link.attr("href");
                // 判断可用网址
                if (!linkUrl.startsWith("http")) {
                    continue;
                }
                // 判断域名相同
                if (sameURL(url, linkUrl)) {
                    continue;
                }
                // 判断访问过
                if (visitedUrls.contains(linkUrl)) {
                    try {
                        graph.addEdge(url + "-" + linkUrl, url, linkUrl, true);
                    } catch (Exception e) {
//                        System.out.println("已经有这条边");
                    }
                    continue;
                }
                externalUrlsCount++;
                // 访问外部网址数量够了
                if (externalUrlsCount >= MAX_EXTERNAL_URLS) {
                    continue;
                }
                // 没访问过就添加进入哈希表
                visitedUrls.add(linkUrl);
                Node node = graph.addNode(linkUrl);
                node.addAttribute("ui.label", node.getId());
                graph.addEdge(url + "-" + linkUrl, url, linkUrl, true);
//                System.out.println(url + "->" + linkUrl);
                jumpUrl(linkUrl, graph, visitedUrls, hops + 1);
            }
        } catch (IOException e) {
//            System.out.println("连接" + url + "出问题~");
        }
    }
}
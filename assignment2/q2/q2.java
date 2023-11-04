package org.cn.edu.tongji;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Main {
    private static final int MAX_HOPS = 3;
    private static final int MAX_EXTERNAL_URLS = 6;
    private static final int CONNECTION_TIMEOUT_MS = 5000;

    public static void main(String[] args) {
//        String[] seedUrls = {"https://www.tongji.edu.cn", "https://www.pku.edu.cn", "http://www.sina.com.cn", "https://www.mit.edu"};
        String[] seedUrls = {"https://www.tongji.edu.cn", "https://www.pku.edu.cn", "http://www.sina.com.cn"};

        Graph graph = new SingleGraph("WebCrawler");
        Set<String> visitedUrls = new HashSet<>();

        long startTime = System.currentTimeMillis();

        for (String seedUrl: seedUrls) {
            Node node = graph.addNode(seedUrl);
            node.addAttribute("ui.label", node.getId());
            visitedUrls.add(seedUrl);
            jumpUrl(seedUrl, graph, visitedUrls, 0);
        }

        long endTime = System.currentTimeMillis();

        printGraphStats(graph);
        System.out.println("花费时间：" + (endTime - startTime) + "ms");
        graph.display();
    }

    private static void printGraphStats(Graph graph) {
        int nodeCount = graph.getNodeCount();
        int edgeCount = graph.getEdgeCount();

        Node maxInDegreeNode = null;
        int maxInDegree = -1;
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

    public static boolean isValidUrl(String url) {
        // 匹配网址的正则表达式
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
                + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";
        Pattern pattern = Pattern.compile(regex);
        // 进行正则匹配判断
        return pattern.matcher(url).matches();
    }

    public static boolean sameURL(String url1, String url2) {
        try {
            URL parsedURL1 = new URL(url1);
            URL parsedURL2 = new URL(url2);

            String host1 = parsedURL1.getHost(); // 提取域名部分
            String[] host1Parts = host1.split("\\.");

            String host2 = parsedURL2.getHost();
            String[] host2Parts = host2.split("\\.");

            if (host1Parts.length < 3 || host2Parts.length < 3) {
                return false;
            }

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
//        System.out.println("current is " + url);
        if (hops > MAX_HOPS) {
            return;
        }
        try {
            URL url1 = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url1.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT_MS);


            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
                reader.close();

                Document document = Jsoup.parse(content.toString());
                Elements links = document.select("a[href]");



                int externalUrlsCount = 0;
                for (Element link: links) {
                    String linkUrl = link.absUrl("href");
                    if (!isValidUrl(linkUrl)) {
                        continue;
                    }
                    if (visitedUrls.contains(linkUrl)) {
                        continue;
                    }
                    if (sameURL(url, linkUrl)) {
                        continue;
                    }
                    visitedUrls.add(linkUrl);
                    if (!linkUrl.isBlank() && externalUrlsCount < MAX_EXTERNAL_URLS) {

                        externalUrlsCount++;
                        // 添加节点时，在节点的标签中添加对应的图中节点ID
                        Node node = graph.addNode(linkUrl);
                        node.addAttribute("ui.label", node.getId());
                        graph.addEdge(url + "-" + linkUrl, url, linkUrl, true);
                        jumpUrl(linkUrl, graph, visitedUrls, hops + 1);
                    }
                }
            }

            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            if (!(e instanceof java.net.SocketTimeoutException)) {
                throw new RuntimeException(e);
            }
        }

    }
}

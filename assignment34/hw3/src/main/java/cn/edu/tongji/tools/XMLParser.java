package cn.edu.tongji.tools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XMLParser {

    public static void main(String[] args) {
        String xmlFilePath = "./test.xml";
        // 解析 xml 建立 author 到 year 到 publications 的映射的映射
//        HashMap<String, HashMap<Integer, Integer>> authorData = parseXMLWithWholeFile(xmlFilePath);
        HashMap<String, HashMap<Integer, Integer>> authorData = parseXMLWithLine(new File(xmlFilePath));
        // 打印信息
        printData(authorData);
        // 保存文件
        saveToFile(authorData, "data.ser");
        // 读取文件
        var temp = loadFromFile("data.ser");
        printData(temp);
    }

    // 解析 xml 获取其中我们需要的信息
    public static HashMap<String, HashMap<Integer, Integer>> parseXMLWithWholeFile(String xmlFilePath) {
        HashMap<String, HashMap<Integer, Integer>> authorData = new HashMap<>();

        try {
            File inputFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList articles = doc.getElementsByTagName("article");
            NodeList books = doc.getElementsByTagName("book");

            processNodeList(articles, authorData);
            processNodeList(books, authorData);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return authorData;
    }

    private static HashMap<String, HashMap<Integer, Integer>> parseXMLWithLine(File file) {
        HashMap<String, HashMap<Integer, Integer>> res = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            System.out.println(file.getName() + " start");
            String line;
            List<String> authors = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                if (line.contains("<article") || line.contains("<book")) {
                    authors.clear();
                }

                if (line.contains("<author")) {
                    Matcher matcher = Pattern.compile("<author.*?>(.*?)</author>").matcher(line);
                    while (matcher.find()) {
                        String author = matcher.group(1);
                        authors.add(author);
                    }
                }

                if (line.contains("<year>")) {
                    Matcher matcher = Pattern.compile("<year>(\\d+)</year>").matcher(line);
                    while (matcher.find()) {
                        int year = Integer.parseInt(matcher.group(1));
                        for (String author : authors) {
                            res.computeIfAbsent(author, k -> new HashMap<>())
                                    .merge(year, 1, Integer::sum);
                        }
                    }
                }
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    private static void processNodeList(NodeList nodeList, HashMap<String, HashMap<Integer, Integer>> authorData) {
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                // 获取作者信息
                NodeList authorList = element.getElementsByTagName("author");
                for (int j = 0; j < authorList.getLength(); j++) {
                    String author = authorList.item(j).getTextContent();

                    // 获取年份信息
                    int year = Integer.parseInt(element.getElementsByTagName("year").item(0).getTextContent());

                    // 更新HashMap数据结构
                    if (authorData.containsKey(author)) {
                        HashMap<Integer, Integer> yearData = authorData.get(author);
                        yearData.put(year, yearData.getOrDefault(year, 0) + 1);
                    } else {
                        HashMap<Integer, Integer> yearData = new HashMap<>();
                        yearData.put(year, 1);
                        authorData.put(author, yearData);
                    }
                }
            }
        }
    }

    private static void printData(HashMap<String, HashMap<Integer, Integer>> authorData) {
        for (String author : authorData.keySet()) {
            System.out.println("Author: " + author);
            HashMap<Integer, Integer> yearData = authorData.get(author);
            for (int year : yearData.keySet()) {
                int publicationCount = yearData.get(year);
                System.out.println("  Year: " + year + " | Publications: " + publicationCount);
            }
            System.out.println();
        }
    }

    private static void saveToFile(HashMap<String, HashMap<Integer, Integer>> data, String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(data);
            System.out.println("Data saved to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, HashMap<Integer, Integer>> loadFromFile(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (HashMap<String, HashMap<Integer, Integer>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}


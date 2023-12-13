package cn.edu.tongji.tools;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;

public class XMLParser {

    public static void main(String[] args) {
        String xmlFilePath = "./test.xml";
        HashMap<String, HashMap<Integer, Integer>> authorData = parseXML(xmlFilePath);
        printAuthorData(authorData);
    }

    public static HashMap<String, HashMap<Integer, Integer>> parseXML(String xmlFilePath) {
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

    private static void printAuthorData(HashMap<String, HashMap<Integer, Integer>> authorData) {
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
}


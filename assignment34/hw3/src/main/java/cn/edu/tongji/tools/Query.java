package cn.edu.tongji.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Query {
    private static final String FILE_NAME = "dblp_line_processed_chunk_1";
    private static final String DATA_FILE_PATH = "dblp_line_processed_chunk_1_bucket_";

    public static String queryByName(String name) {
        try {
            PersistentBTree<String, Long>[] trees = new PersistentBTree[4];
            long startTime = System.nanoTime();
            for (int i = 0; i < 4; ++i) {
                trees[i] = PersistentBTree.loadFromFile(FILE_NAME + "_bucket_" + i + "_index_tree.ser");
            }
            long endTime = System.nanoTime();
            System.out.println("读取index文件用时： " + (endTime - startTime) / 1_000_000);

            int hashResult = customHashFunction(name) % 4;

            Long pointer = trees[hashResult].search(name);
            if (pointer != null) {
                startTime = System.nanoTime();
                try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE_PATH + hashResult + ".lson"))) {
                    reader.skip(pointer);
                    String jsonData = reader.readLine();
                    System.out.println(jsonData);
                    return jsonData;
                } catch (IOException e) {
                    System.out.println("Failed to read data file: " + e.getMessage());
                }
                endTime = System.nanoTime();
                System.out.println("本次查询用时： " + (endTime - startTime) / 1_000_000);
            } else {
                System.out.println("Name not found");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load BTree from file: " + e.getMessage());
        }
        return "query failed";
    }

    public static void main(String[] args) {
        try {
            PersistentBTree<String, Long>[] trees = new PersistentBTree[4];
            long startTime = System.nanoTime();
            for (int i = 0; i < 4; ++i) {
                trees[i] = PersistentBTree.loadFromFile(FILE_NAME + "_bucket_" + i + "_index_tree.ser");
            }
            long endTime = System.nanoTime();
            System.out.println("读取index文件用时： " + (endTime - startTime) / 1_000_000);

            try (Scanner sc = new Scanner(System.in)) {
                while (true) {
                    System.out.println("输入你要查询的姓名: ");
                    String name = sc.nextLine();
                    if (name.equals("q")) {
                        break;
                    }

                    int hashResult = customHashFunction(name) % 4;

                    Long pointer = trees[hashResult].search(name);
                    if (pointer != null) {
                        startTime = System.nanoTime();
                        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE_PATH + hashResult + ".lson"))) {
                            reader.skip(pointer);
                            String jsonData = reader.readLine();
                            System.out.println(jsonData);
                        } catch (IOException e) {
                            System.out.println("Failed to read data file: " + e.getMessage());
                        }
                        endTime = System.nanoTime();
                        System.out.println("本次查询用时： " + (endTime - startTime) / 1_000_000);

                    } else {
                        System.out.println("Name not found");
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load BTree from file: " + e.getMessage());
        }
    }

    public static int customHashFunction(String name) {
        // 初始化哈希值
        int hashValue = 0;

        // 遍历姓名中的每个字符
        for (char ch : name.toCharArray()) {
            // 将字符的Unicode码值加到哈希值中
            hashValue += (int) ch;
        }

        return hashValue;
    }
}

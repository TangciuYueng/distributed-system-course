package cn.edu.tongji.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Query {
    private static final String FILE_NAME = "dbpl_data.ser";
    private static final String DATA_FILE_PATH = "dblp_line.lson";

    public static void main(String[] args) {
        try {
            PersistentBTree<String, Long> loadedBTree = PersistentBTree.loadFromFile(FILE_NAME);
            System.out.println("BTree loaded from file:");

            try (Scanner sc = new Scanner(System.in)) {
                while (true) {
                    System.out.println("输入你要查询的姓名: ");
                    String name = sc.nextLine();
                    if (name.equals("q")) {
                        break;
                    }

                    Long pointer = loadedBTree.search(name);
                    if (pointer != null) {
                        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_FILE_PATH))) {
                            reader.skip(pointer);
                            String jsonData = reader.readLine();
                            System.out.println(jsonData);
                        } catch (IOException e) {
                            System.out.println("Failed to read data file: " + e.getMessage());
                        }
                    } else {
                        System.out.println("Name not found");
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load BTree from file: " + e.getMessage());
        }
    }
}

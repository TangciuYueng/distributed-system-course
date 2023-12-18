package cn.edu.tongji.tools;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

public class CreateIndex {
    static BTree<String, Long> createBTree(String filePath) {
        var namePointers = GetNamePosition.getNamePointer(filePath);
        BTree<String, Long> bTree = new BTree<>();
        for (Map.Entry<String, Long> entry: namePointers.entrySet()) {
            bTree.insert(entry.getKey(), entry.getValue());
        }
        return bTree;
    }

    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        String filePath = sc.nextLine();
        String filePath = "D:\\大学学习资料\\大三上学期学习\\分布式系统\\作业\\dblp.xml\\dblp_line.lson";

        PersistentBTree bTree = createPersistentBTree(filePath);
//        bTree.output();
        String fileName = "dbpl_data.ser";
        try {
            bTree.saveToFile(fileName);
            System.out.println("success!");
        } catch (IOException e) {
            System.out.println("failed to ");
        }
    }

    private static PersistentBTree createPersistentBTree(String filePath) {
        var namePointers = GetNamePosition.getNamePointer(filePath);
        PersistentBTree<String, Long> bTree = new PersistentBTree<>();
        for (Map.Entry<String, Long> entry: namePointers.entrySet()) {
            bTree.insert(entry.getKey(), entry.getValue());
        }
        return bTree;
    }
}

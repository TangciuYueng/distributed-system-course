package cn.edu.tongji.tools;

import java.util.Map;
import java.util.Scanner;

public class CreateIndex {
    static BTree<String, Long> create(String filePath) {
        var namePointers = GetNamePosition.getNamePointer(filePath);
        BTree<String, Long> bTree = new BTree<>();
        for (Map.Entry<String, Long> entry: namePointers.entrySet()) {
            bTree.insert(entry.getKey(), entry.getValue());
        }
        return bTree;
    }



    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String filePath = sc.nextLine();

        var bTree = create(filePath);
        bTree.output();
    }
}

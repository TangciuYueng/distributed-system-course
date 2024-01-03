package cn.edu.tongji.tools;

import java.io.IOException;
import java.nio.file.Paths;
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
        String str1= "D:\\大学学习资料\\大三上学期学习\\分布式系统\\distributed-system-course\\assignment34\\hw3\\dblp_line_processed_chunk_";
        String str2="_bucket_";
        String str3=".lson";
        for (int i = 1; i <= 7; i++) {
            for (int j = 0; j < 4; j++) {
                process(str1+i+str2+j+str3);
            }
        }
    }

    public static void process(String filePath) {
//        Scanner sc = new Scanner(System.in);
//        String filePath = sc.nextLine();
//        String filePath = "D:\\大学学习资料\\大三上学期学习\\分布式系统\\作业\\dblp.xml\\dblp_line.lson";


        PersistentBTree bTree = createPersistentBTree(filePath);
//        bTree.output();

        // 获取文件名
        String fileNameWithExtension = Paths.get(filePath).getFileName().toString();

        // 获取没有后缀的文件名
        String fileNameWithoutExtension = getFileNameWithoutExtension(fileNameWithExtension);

        String fileName = fileNameWithoutExtension + "_index_tree.ser";
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

    private static String getFileNameWithoutExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}

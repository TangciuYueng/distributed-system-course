package cn.edu.tongji.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class GetNamePosition {
    public static Map<String, Long> getNamePointer(String filePath) {
        Map<String, Long> namePointers = new HashMap<>();

        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            long currentPos = 0;
            String line = file.readLine();

            while (line != null) {
                // 记录当前姓名对应的起始位置
                String name = line.substring(2, line.indexOf("\":"));
                namePointers.put(name, currentPos);

                // 更新当前位置，准备读取下一行
                currentPos = file.getFilePointer();
                line = file.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // 打印姓名到记录起始位置的映射关系
//        for (Map.Entry<String, Long> entry : namePointers.entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
        return namePointers;
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String filePath = sc.nextLine();

        var namePointers = getNamePointer(filePath);

        // 验证
        String givenName = "mvv";
        if (namePointers.containsKey(givenName)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                reader.skip(namePointers.get(givenName));
                String jsonData = reader.readLine();
                System.out.println(jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("no this name");
        }
    }
}

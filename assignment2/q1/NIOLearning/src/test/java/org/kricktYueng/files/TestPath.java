package org.kricktYueng.files;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestPath {
    public static void main(String[] args) {
        Path path = Paths.get("words2.txt");
        System.out.println(path);
        // 拼接
        Path path1 = Paths.get("./folder", "words2.txt");
        System.out.println(path1);
        // 正常化
        System.out.println(path1.normalize());
    }
}

package org.kricktYueng.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestWalkCopyDir {
    public static void main(String[] args) {
        String source = "Hello";
        String target = "Hello-copy";

        try {
            // 遍历这个文件夹下的每个 path
            Files.walk(Paths.get(source)).forEach(path -> {
                // 替换成目标路径
                String targetName = path.toString().replace(source, target);
                try {
                    Path dir = Paths.get(targetName);
                    // 文件夹就创建
                    if (Files.isDirectory(path)) {
                        Files.createDirectory(dir);
                        // 文件就复制
                    } else if (Files.isRegularFile(path)) {
                        Files.copy(path, dir);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.kricktYueng.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class TestFiles {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("Hello/d1");
        // 判断是否存在
        System.out.println(Files.exists(path));
        // 创建多级目录
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        // 拷贝文件
        Path source = Paths.get("words.txt");
        Path target = Paths.get("words3.txt");
        // 如果文件已经存在会抛出异常
//        Files.copy(source, target);
        // 想要覆盖已存在文件
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

        // 移动文件
        // 保证移动的原子性
        Files.move(target, path, StandardCopyOption.ATOMIC_MOVE);

        // 删除文件
        // 文件不存在就抛出异常NoSuchFileException
        Files.delete(path);
        // 删除目录
        // 目录还有内存就抛出异常DirectoryNotEmptyException
    }
}

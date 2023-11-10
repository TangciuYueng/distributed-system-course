package org.cn.edu.tongji.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ReceiveFile {
    private DataInputStream dataInputStream;
    private String basePath;
    private int fileNameLength;
    private static final int BUFFER_SIZE = 4096;
    public ReceiveFile(DataInputStream dataInputStream, String basePath, int fileNameLength) {
        this.dataInputStream = dataInputStream;
        this.basePath = basePath;
        this.fileNameLength = fileNameLength;
    }
    // 文件接收并写入目标文件夹
    public void receive() throws IOException {
        // 接收文件名
        byte[] fileNameBuffer = new byte[fileNameLength];
        dataInputStream.read(fileNameBuffer);
        String fileName = new String(fileNameBuffer, "UTF-8");
        System.out.println("received " + fileName);
        // 接收文件长度
        long fileLength = dataInputStream.readLong();
        System.out.println("file length " + fileLength);
        // 构建文件存储目录
        Path filePath = Paths.get(basePath, fileName);
        // 创建目录
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            // 接受文件内容
            byte[] buffer = new byte[BUFFER_SIZE];
            // 记录已经读取的字节数量
            int totalBytesRead = 0;
            // 保证最多只读取fileLength个字节
            while (totalBytesRead < fileLength) {
                int bytesRead = dataInputStream.read(buffer, 0, Math.min(buffer.length, (int) (fileLength - totalBytesRead)));
                if (bytesRead == -1) {
                    break;
                }
                totalBytesRead += bytesRead;
                // 写入文件
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                fileChannel.write(byteBuffer);
            }
        }

    }
}

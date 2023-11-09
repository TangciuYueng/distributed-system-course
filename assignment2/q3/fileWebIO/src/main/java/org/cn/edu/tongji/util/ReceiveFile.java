package org.cn.edu.tongji.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ReceiveFile {
    private int fileNameLength;
    private SocketChannel socketChannel;
    private String basePath;
    private static final int BUFFER_SIZE = 4096;

    public ReceiveFile(SocketChannel socketChannel, int fileNameLength, String basePath) {
        this.socketChannel = socketChannel;
        this.fileNameLength = fileNameLength;
        this.basePath = basePath;
    }

    public void receive() throws IOException {
        // 接收文件名
        ByteBuffer fileNameBuffer = ByteBuffer.allocate(fileNameLength);
        int bytesRead = socketChannel.read(fileNameBuffer);
        fileNameBuffer.flip();
        byte[] fileNameBytes = new byte[bytesRead];
        fileNameBuffer.get(fileNameBytes);
        String fileName = new String(fileNameBytes);
        fileName = fileName.trim();
        System.out.println("接收到文件名：" + fileName);

        // 接收文件长度
        ByteBuffer fileLengthBuffer = ByteBuffer.allocate(Long.BYTES);
        socketChannel.read(fileLengthBuffer);
        fileLengthBuffer.flip();
        long fileLength = fileLengthBuffer.getLong();
        System.out.println("文件长度为: " + fileLength);

        // 构建完整的文件保存路径
        Path filePath = Paths.get(basePath, fileName);

        // 创建目录
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // 接收文件内容
        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            // 已经读取的字节数
            long fileBytesRead = 0;
            ByteBuffer fileBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            // 直到读完文件内容停止 分批次读取写入
            while (fileBytesRead < fileLength) {
                // 还未读取的字节数
                long remaining = fileLength - fileBytesRead;
                // 取剩下未读取的字节和BUFFER_SIZE中最小的
                int toReadSize = (int) Math.min(remaining, BUFFER_SIZE);
                // 限制缓冲区大小
                fileBuffer.limit(toReadSize);
                // 读取socket
                int fileBytesReadNow = socketChannel.read(fileBuffer);
                if (fileBytesReadNow == -1) {
                    break;
                }
                fileBuffer.flip();
                // 写入文件
                fileChannel.write(fileBuffer);
                fileBytesRead += fileBytesReadNow;
                fileBuffer.clear();
            }
            System.out.println("文件接收完成：" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

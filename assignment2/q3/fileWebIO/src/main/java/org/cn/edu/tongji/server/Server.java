package org.cn.edu.tongji.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;

public class Server implements Runnable {
    private static final int BUFFER_SIZE = 4 * 1024;
    private int serverPort;
    private String basePath;

    public Server(int serverPort, String basePath) {
        this.serverPort = serverPort;
        this.basePath = basePath;
    }

    @Override
    public void run() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            // 绑定端口
            serverSocketChannel.bind(new InetSocketAddress(serverPort));
            System.out.println("服务器已启动，等待客户端连接...");

            while (true) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.println("客户端已连接：" + socketChannel.getRemoteAddress());
                handleClient(socketChannel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(SocketChannel socketChannel) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(Character.BYTES);
            socketChannel.read(buffer);
            buffer.flip();
            char request = buffer.getChar();

            if (request == 'U') {
                handleUpload(socketChannel);
            } else if (request == 'D') {
                handleDownload(socketChannel);
            } else {
                System.out.println("请求类型错误：" + request);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDownload(SocketChannel socketChannel) throws IOException {
        try {
            while (true) {
                // 接收文件名长度
                ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                if (socketChannel.read(fileNameLengthBuffer) < 0) {
                    break;
                }
                fileNameLengthBuffer.flip();
                int fileNameLength = fileNameLengthBuffer.getInt();
                System.out.println("文件名长度为: " + fileNameLength);

                // 接收文件名
                String fileName = receiveFileName(socketChannel, fileNameLength);
                System.out.println("接收到文件名：" + fileName);

                // 构建完整的文件保存路径
                Path filePath = Paths.get(basePath, fileName);

                // 发送文件
                sendFile(socketChannel, filePath, fileName);
                System.out.println("文件" + fileName + "发送成功");

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendFile(SocketChannel socketChannel, Path filePath, String fileName) {
        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            // 文件名转换成为字节数组
            byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
            // 发送文件名长度
            ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
            fileNameLengthBuffer.putInt(fileNameBytes.length);
            fileNameLengthBuffer.flip();
            socketChannel.write(fileNameLengthBuffer);
            // 发送文件名
            ByteBuffer fileNameBuffer = ByteBuffer.wrap(fileNameBytes);
            fileNameBuffer.flip();
            socketChannel.write(fileNameBuffer);
            fileNameBuffer.clear();

            // 发送文件内容长度
            ByteBuffer fileLengthBuffer = ByteBuffer.allocate(Long.BYTES);
            fileLengthBuffer.putLong(fileChannel.size());
            fileLengthBuffer.flip();
            socketChannel.write(fileLengthBuffer);
            // 发送文件内容
            fileChannel.transferTo(0, fileChannel.size(), socketChannel);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String receiveFileName(SocketChannel socketChannel, int fileNameLength) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(fileNameLength);
        int bytesRead = socketChannel.read(buffer);
        buffer.flip();
        byte[] fileNameBytes = new byte[bytesRead];
        buffer.get(fileNameBytes);
        String fileName = new String(fileNameBytes);
        fileName = fileName.trim();
        return fileName;
    }

    private void handleUpload(SocketChannel socketChannel) {
        try {
            while (true) {
                // 接收文件名长度
                ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                if (socketChannel.read(fileNameLengthBuffer) < 0) {
                    break;
                }
                fileNameLengthBuffer.flip();
                int fileNameLength = fileNameLengthBuffer.getInt();
                System.out.println("文件名长度为: " + fileNameLength);

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
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
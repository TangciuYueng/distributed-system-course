package org.cn.edu.tongji.server;

import org.cn.edu.tongji.util.ReceiveFile;
import org.cn.edu.tongji.util.SendFile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Server implements Runnable {
    private int serverPort;
    private String basePath;
    private ArrayList<String> downloadReceivedFileName = new ArrayList<>();

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

                // 保存进入数组
                downloadReceivedFileName.add(fileName);
            }

            // 根据得到的文件名发送文件
//            for (String fileName: downloadReceivedFileName) {
//                // 构建完整的文件保存路径
//                Path filePath = Paths.get(basePath, fileName);
//                try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
//                    // 发送文件
//                    SendFile sendFile = new SendFile(fileName, fileChannel, socketChannel);
//                    sendFile.send();
//                    System.out.println("文件" + fileName + "发送成功");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
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
                // 得到文件名长度后接收文件并写入磁盘
                ReceiveFile receiveFile = new ReceiveFile(socketChannel, fileNameLength, basePath);
                receiveFile.receive();
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
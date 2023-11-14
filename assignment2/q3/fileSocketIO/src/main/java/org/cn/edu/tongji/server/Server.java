package org.cn.edu.tongji.server;

import org.cn.edu.tongji.util.ReceiveFile;
import org.cn.edu.tongji.util.SendFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server implements Runnable {
    private int serverPort;
    private String basePath;
    // 保证线程安全
    private ConcurrentLinkedQueue<String> chunkfileNames = new ConcurrentLinkedQueue<>();

    public Server(int serverPort, String basePath) {
        this.serverPort = serverPort;
        this.basePath = basePath;
    }


    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("server is on " + serverPort);
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("client connected: " + socket.getInetAddress() + serverPort);
                    handleClient(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("完成一次连接");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
            char request = (char) dataInputStream.readByte();
            if (request == 'U') {
                handleUpload(dataInputStream);
            } else if (request == 'D') {
                handleDownload(dataInputStream, dataOutputStream);
            } else if (request == 'P') {
                handleCUpload(socket);
            } else if (request == 'G') {
                handleCDownload(socket);
            } else {
                System.out.println("request error");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCDownload(Socket socket) {
        try {
            CDownload cDownload = new CDownload(socket, basePath);
            cDownload.handleCDownload();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCUpload(Socket socket) {
        try {
            CUpload cUpload = new CUpload(socket, basePath);
            cUpload.handleCUpload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDownload(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        // 接收文件名
        while (true) {
            try {
                int fileNameLength = dataInputStream.readInt();
                byte[] chunkName = new byte[fileNameLength];
                dataInputStream.readFully(chunkName);
                chunkfileNames.add(new String(chunkName, "UTF-8"));
                System.out.println(serverPort + " receive file name " + new String(chunkName, "UTF-8"));
            } catch (Exception e) {
                break;
            }
        }
        System.out.println("file name receiving finish");
        // 发送文件
        for (String fileName: chunkfileNames) {
            System.out.println("send fileName" + fileName);
            Path filePath = Paths.get(basePath, fileName);
            System.out.println("filePath " + filePath);
            try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
                SendFile sendFile = new SendFile(fileName, fileChannel, dataOutputStream);
                sendFile.send();

                System.out.println("发送文件成功" + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleUpload(DataInputStream dataInputStream) throws IOException {
        while (true) {
            int fileNameLength;
            try {
                // 接收文件名长度
                fileNameLength = dataInputStream.readInt();
                System.out.println("file name length " + fileNameLength);
            } catch (Exception e) {
                // 结束接收
                break;
            }
            // 接收并写入文件
            ReceiveFile receiveFile = new ReceiveFile(dataInputStream, basePath, fileNameLength);
            receiveFile.receive();
        }
    }
}

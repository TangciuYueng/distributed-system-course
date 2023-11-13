package org.cn.edu.tongji.server;

import org.cn.edu.tongji.util.ReceiveFile;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CUpload {
    private String fileName;
    private String fileExt;
    private Socket socket;
    private String basePath;
    private List<Integer> allocatedChunks;
    private List<String> requiredChunkNames;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public CUpload(Socket socket, String basePath) throws IOException {
        this.socket = socket;
        this.basePath = basePath;
        requiredChunkNames = new ArrayList<>();
        allocatedChunks = new ArrayList<>();
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataInputStream = new DataInputStream(socket.getInputStream());
    }
    // 接收文件名
    private void getFileName() throws IOException {
        int fileNameLength = dataInputStream.readInt();
        byte[] fileNameByte = new byte[fileNameLength];
        dataInputStream.readFully(fileNameByte);
        this.fileName = new String(fileNameByte, StandardCharsets.UTF_8);

        Path path = Paths.get(fileName);
        this.fileName = path.getFileName().toString();
        this.fileExt = "";
        int dotIndex = this.fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            this.fileExt = this.fileName.substring(dotIndex + 1);
            this.fileName = this.fileName.substring(0, dotIndex);
        }
        System.out.println(fileName);
    }
    // 获取分配要接收的块号
    private void getAllocation() throws IOException {
        int chunkCount = dataInputStream.readInt();
        for (int i = 0; i < chunkCount; ++i) {
            allocatedChunks.add(dataInputStream.readInt());
        }
    }
    // 检查已有块号
    private void checkChunk() {
        for (Integer chunkNum: allocatedChunks) {
            String chunkFileName = fileName + chunkNum + "." + fileExt;
            Path filePath = Paths.get(basePath, chunkFileName);
            System.out.println("检查" + filePath);
            if (!Files.exists(filePath)) {
                requiredChunkNames.add(chunkFileName);
            }
        }
    }
    // 发送还需要接收的块文件名
    private void sendRequiredChunkName() throws IOException {
        for (String chunkFileName: requiredChunkNames) {
            byte[] fileNameByte = chunkFileName.getBytes();
            dataOutputStream.writeInt(fileNameByte.length);
            dataOutputStream.write(fileNameByte);
        }
    }
    // 接收块文件
    private void getChunk() throws IOException {
        while (true) {
            int fileNameLength;
            try {
                // 接收文件名长度
                fileNameLength = dataInputStream.readInt();
                // 设置超时防止阻塞
                socket.setSoTimeout(3000);
                System.out.println("file name length " + fileNameLength);
            } catch (Exception e) {
                // 结束接收
                break;
            }
            // 接收并写入文件
            ReceiveFile receiveFile = new ReceiveFile(dataInputStream, basePath, fileNameLength);
            receiveFile.receive();
            System.out.println("写入成功");
        }
    }
    public void handleCUpload() throws IOException {
        // 接收文件名
        getFileName();
        // 获取分配要接收的块号
        getAllocation();
        // 检查已有块号
        checkChunk();
        // 发送还需要接收的块文件名
        sendRequiredChunkName();
        // 接收块文件
        getChunk();
        // 释放资源
        releaseStream();
    }

    private void releaseStream() throws IOException {
        dataInputStream.close();
        dataOutputStream.close();
    }
}

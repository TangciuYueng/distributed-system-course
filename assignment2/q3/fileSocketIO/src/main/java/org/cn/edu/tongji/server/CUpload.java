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

    public CUpload(Socket socket, String basePath) {
        this.socket = socket;
        this.basePath = basePath;
        requiredChunkNames = new ArrayList<>();
    }
    // 接收文件名
    private void getFileName() throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
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
    }
    // 获取分配要接收的块号
    private void getAllocation() throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

        int byteArrayLength = objectInputStream.readInt();
        byte[] byteArray = new byte[byteArrayLength];
        objectInputStream.readFully(byteArray);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        ObjectInputStream objectInputStream1 = new ObjectInputStream(byteArrayInputStream);

        allocatedChunks = (List<Integer>) objectInputStream1.readObject();
    }
    // 检查已有块号
    private void checkChunk() {
        for (Integer chunkNum: allocatedChunks) {
            String chunkFileName = fileName + chunkNum + "." + fileExt;
            Path filePath = Paths.get(basePath, chunkFileName);

            if (!Files.exists(filePath)) {
                requiredChunkNames.add(chunkFileName);
            }
        }
    }
    // 发送还需要接收的块文件名
    private void sendRequiredChunkName() throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        for (String chunkFileName: requiredChunkNames) {
            byte[] fileNameByte = chunkFileName.getBytes();
            dataOutputStream.writeInt(fileNameByte.length);
            dataOutputStream.write(fileNameByte);
        }
    }
    // 接收块文件
    private void getChunk() throws IOException {
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

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
    public void handleCUpload() throws IOException, ClassNotFoundException {
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
    }
}

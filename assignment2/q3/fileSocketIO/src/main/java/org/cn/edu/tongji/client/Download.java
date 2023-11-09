package org.cn.edu.tongji.client;

import org.cn.edu.tongji.util.ReceiveFile;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Download {
    private final String hashTableFilePath;
    private String fileName;
    private String basePath;
    private String fileExt;
    private int chunkCount;
    private List<Integer> serverPort = new ArrayList<>();
    private HashMap<Integer, List<Integer>> hash;
    private static final String request = "D";
    private static final String SERVER_HOST = "localhost";
    public Download(String fileName) {
        this.fileName = fileName;
        int dotIndex = this.fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            this.fileExt = this.fileName.substring(dotIndex + 1);
            this.fileName = this.fileName.substring(0, dotIndex);
        } else {
            this.fileExt = "";
        }
        // 哈希表文件需要文件原本的后缀
        this.hashTableFilePath = fileName + ".ser";
        getHashTable();
        getPort();
        getChunkCount();
        // 下载存入文件夹的路径
        basePath = "files_download";
    }
    private void getHashTable() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Path.of(hashTableFilePath), StandardOpenOption.READ)
        )) {
            hash = (HashMap<Integer, List<Integer>>) objectInputStream.readObject();
            System.out.println("successfully read hashtable: " + hash);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void getPort() {
        for (Integer key: hash.keySet()) {
            serverPort.add(key);
        }
    }
    private void getChunkCount() {
        for (List<Integer> val: hash.values()) {
            chunkCount += val.size();
        }
    }

    private void mergeFile() {
        Path targetFilePath = Paths.get(basePath, fileName + "." + fileExt);
        Path parentDir = targetFilePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileChannel fileChannel = FileChannel.open(targetFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < chunkCount; ++i) {
                try (FileChannel chunkFileChannel = FileChannel.open(Path.of(fileName + i + "." + fileExt), StandardOpenOption.READ)) {
                    fileChannel.position(fileChannel.size());
                    chunkFileChannel.transferTo(0, chunkFileChannel.size(), fileChannel);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getChunk() {
        Socket[] sockets = new Socket[serverPort.size()];
        try {
            // 发送要下载的文件名
            for (int i = 0; i < sockets.length; ++i) {
                sockets[i] = new Socket(SERVER_HOST, serverPort.get(i));
                DataOutputStream dataOutputStream = new DataOutputStream(sockets[i].getOutputStream());
                dataOutputStream.write(request.getBytes(StandardCharsets.UTF_8));

                // 发送给对应端口要下载的chunk文件名
                for (Integer chunkIndex : hash.get(serverPort.get(i))) {
                    String chunkFileName = fileName + chunkIndex + "." + fileExt;
                    byte[] chunkFileNameBytes = chunkFileName.getBytes(StandardCharsets.UTF_8);
                    // 发送文件名长度
                    dataOutputStream.writeInt(chunkFileNameBytes.length);
                    // 发送文件名
                    dataOutputStream.write(chunkFileNameBytes);
                    System.out.println("send file name " + chunkFileName);
                }
                // 关闭输出流 防止阻塞
                sockets[i].shutdownOutput();
            }

            // 接收文件
            for (int i = 0; i < sockets.length; ++i) {
                DataInputStream dataInputStream = new DataInputStream(sockets[i].getInputStream());
                // 接收
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
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接
            for (int i = 0; i < sockets.length; ++i) {
                if (sockets[i] != null && !sockets[i].isClosed()) {
                    try {
                        sockets[i].close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Download download = new Download("test.pdf");
        download.getChunk();
        download.mergeFile();
    }
}

package org.cn.edu.tongji.client;

import org.cn.edu.tongji.util.ReceiveFile;

import java.io.*;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Download {
    protected final String hashTableFilePath;
    protected String fileName;
    protected String basePath;
    protected String fileExt;
    protected int chunkCount;
    protected List<Integer> serverPort = new ArrayList<>();
    protected HashMap<Integer, List<Integer>> hash;
    private static final String request = "D";
    protected static final String SERVER_HOST = "localhost";
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

        // 下载存入文件夹的路径
        basePath = "files_download";
    }

    // 读取本地哈希表记录找到对应文件块对应的服务器
    protected void getHashTable() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Path.of(hashTableFilePath), StandardOpenOption.READ)
        )) {
            hash = (HashMap<Integer, List<Integer>>) objectInputStream.readObject();
//            System.out.println("successfully read hashtable: " + hash);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // 获得端口信息
    protected void getPort() {
        serverPort.addAll(hash.keySet());
    }

    // 获得文件块数
    protected void getChunkCount() {
        chunkCount = hash
                .values()
                .stream()
                .mapToInt(List::size)
                .sum();
    }

    // 合并文件块
    protected void mergeFile() {
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
                Path chunkFilePath = Paths.get(basePath, fileName + i + "." + fileExt);
                try (FileChannel chunkFileChannel = FileChannel.open(chunkFilePath, StandardOpenOption.READ)) {
                    fileChannel.position(fileChannel.size());
                    chunkFileChannel.transferTo(0, chunkFileChannel.size(), fileChannel);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 发送文件块名字并接收文件块
    protected void getChunk() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 每个存有文件块的 发送文件名 接收文件块
        for (int port : serverPort) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try (Socket socket = new Socket(SERVER_HOST, port);
                     DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                     DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())
                     ) {

                    // 发送请求类型
                    dataOutputStream.write(request.getBytes(StandardCharsets.UTF_8));

                    for (int chunkIndex : hash.get(port)) {
                        String chunkFileName = fileName + chunkIndex + "." + fileExt;
                        byte[] chunkFileNameBytes = chunkFileName.getBytes(StandardCharsets.UTF_8);
                        dataOutputStream.writeInt(chunkFileNameBytes.length);
                        dataOutputStream.write(chunkFileNameBytes);
//                        System.out.println("Sent file name: " + chunkFileName);
                    }

                    // 关闭输出流 避免服务器阻塞
                    socket.shutdownOutput();

                    while (true) {
                        int fileNameLength;
                        try {
                            fileNameLength = dataInputStream.readInt();
//                            System.out.println("File name length: " + fileNameLength);
                        } catch (EOFException e) {
                            // 结束接收
                            break;
                        }
                        // 接收文件内容
                        ReceiveFile receiveFile = new ReceiveFile(dataInputStream, basePath, fileNameLength);
                        receiveFile.receive();
                    }
                } catch (IOException e) {
                    // 处理异常
                    throw new RuntimeException("Failed to receive chunk file.", e);
                }
            });

            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
    // 删除文件块
    protected void deleteChunkFile() {
        // 循环读取块的过程放入一个线程池中
        ExecutorService executorService = Executors.newFixedThreadPool(chunkCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < chunkCount; ++i) {
            int chunkIndex = i;
            // 封装为异步任务交给线程池处理
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // 删除临时chunk文件
                Path chunkFilePath = Paths.get(basePath, fileName + chunkIndex + "." + fileExt);
                if (Files.exists(chunkFilePath)) {
                    try {
                        Files.delete(chunkFilePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, executorService);
            futures.add(future);
        }
        // 等待所有任务完成关闭线程池
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
    }

    public static void downloadFile(String fileName) {
        Download download = new Download(fileName);
        try {
            download.getHashTable();
            download.getPort();
            download.getChunkCount();
            download.getChunk();
            download.mergeFile();
            download.deleteChunkFile();
        } catch (Exception e) {
            System.out.println("没有这个文件~");
        }

    }

    public static void main(String[] args) {
        Download download = new Download("test.pdf");
        download.getHashTable();
        download.getPort();
        download.getChunkCount();
        download.getChunk();
        download.mergeFile();
        download.deleteChunkFile();
    }
}

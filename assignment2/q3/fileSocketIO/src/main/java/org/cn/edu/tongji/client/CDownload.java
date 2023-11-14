package org.cn.edu.tongji.client;

import org.cn.edu.tongji.util.ReceiveFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CDownload extends Download {
    private HashMap<Integer, List<String>> requiredChunkFileNames;
    private static final String request = "G";
    public CDownload(String fileName) {
        super(fileName);
        requiredChunkFileNames = new HashMap<>();
    }

    @Override
    protected void getChunk() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        System.out.println(serverPort);
        for (int port: serverPort) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try (Socket socket = new Socket(SERVER_HOST, port);
                     DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                     DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())
                ) {
                    // 发送请求类型
                    dataOutputStream.write(request.getBytes(StandardCharsets.UTF_8));
                    // 发送请求文件名个数和文件名
                    dataOutputStream.writeInt(requiredChunkFileNames.get(port).size());
                    for (String chunkFileName: requiredChunkFileNames.get(port)) {
                        byte[] chunkFileNameBytes = chunkFileName.getBytes(StandardCharsets.UTF_8);
                        dataOutputStream.writeInt(chunkFileNameBytes.length);
                        dataOutputStream.write(chunkFileNameBytes);
                    }
                    // 接收文件
                    for (int i = 0; i < requiredChunkFileNames.get(port).size(); ++i) {
                        int fileNameLength = dataInputStream.readInt();
                        ReceiveFile receiveFile = new ReceiveFile(dataInputStream, basePath, fileNameLength);
                        receiveFile.receive();
                    }
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } ;

            });

            futures.add(future);
        }
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    // 检查还需要哪些块文件
    private void checkRequired() {
        for (Map.Entry<Integer, List<Integer>> entry: hash.entrySet()) {
            int port = entry.getKey();
            for (Integer chunkNumber: entry.getValue()) {
                String chunkFileName = fileName + chunkNumber + "." + fileExt;
                Path chunkFilePath = Path.of(basePath, chunkFileName);
                if (!Files.exists(chunkFilePath)) {
                    if (requiredChunkFileNames.containsKey(port)) {
                        List<String> v = requiredChunkFileNames.get(port);
                        v.add(chunkFileName);
                        requiredChunkFileNames.put(port, v);
                    } else {
                        requiredChunkFileNames.put(port, new ArrayList<>(Arrays.asList(chunkFileName)));
                    }
                }
            }
        }
    }

    public static void CDownloadFile(String fileName) {
        CDownload cDownload = new CDownload(fileName);
        try {
            cDownload.getHashTable();
            cDownload.getPort();
            cDownload.getChunkCount();
            cDownload.checkRequired();
            cDownload.getChunk();
            cDownload.mergeFile();
            cDownload.deleteChunkFile();
        } catch (Exception e) {
            System.out.println("没有这个文件~");
        }

    }

    public static void main(String[] args) {
        CDownload cDownload = new CDownload("test.pdf");
        cDownload.getHashTable();
        cDownload.getPort();
        cDownload.getChunkCount();
        cDownload.checkRequired();
        cDownload.getChunk();
        cDownload.mergeFile();
        cDownload.deleteChunkFile();
    }
}

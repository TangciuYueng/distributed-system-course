package org.cn.edu.tongji.client;

import org.cn.edu.tongji.util.SendFile;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Upload {
    private String filePath;
    private String fileName;
    private String fileExt;
    private String hashTableFilePath;
    private int chunkCount;
    // 端口号到文件块列表的映射
    private HashMap<Integer, List<Integer>> hash;

    private static final String BASE_CHUNK_FILE_PATH = "./";
    private static final String request = "U";
    private static final int CHUNK_SIZE = 1024 * 1024;
    private static String SERVER_HOST = "localhost";
    private static int[] SERVER_PORTS = {8887, 8888, 8889};

    public Upload(String filePath) {
        this.filePath = filePath;
        Path path = Paths.get(filePath);
        this.fileName = path.getFileName().toString();
        this.fileExt = "";
        int dotIndex = this.fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            this.fileExt = this.fileName.substring(dotIndex + 1);
            this.fileName = this.fileName.substring(0, dotIndex);
        }
        hash = new HashMap<>();
        hashTableFilePath = BASE_CHUNK_FILE_PATH + fileName + "." + fileExt + ".ser";
    }
    // 将文件分割成多个块
    public void getChunk() {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
             FileChannel fileChannel = file.getChannel()) {
            // 总共需要处理的字节数
            long fileSize = file.length();
            // 产生的块数
            chunkCount = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
            // 循环读取块的过程放入一个线程池中
            ExecutorService executorService = Executors.newFixedThreadPool(chunkCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (int i = 0; i < chunkCount; ++i) {
                int chunkIndex = i;
                // 封装为异步任务交给线程池处理
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + chunkIndex + "." + fileExt;
                    try (RandomAccessFile chunkFile = new RandomAccessFile(chunkFilePath, "rw");
                         FileChannel chunkFileChannel = chunkFile.getChannel()) {
                        long position = chunkIndex * CHUNK_SIZE;
                        long remaining = fileSize - position;
                        long chunkSize = Math.min(CHUNK_SIZE, remaining);
                        fileChannel.transferTo(position, chunkSize, chunkFileChannel);
                    } catch (Exception e) {
                        System.out.println("创建" + chunkFilePath + "出错");
                    }
                }, executorService);
                futures.add(future);
            }
            // 等待所有任务完成关闭线程池
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executorService.shutdown();

        } catch (Exception e) {
            System.out.println("读取" + filePath + "出错");
            e.printStackTrace();
        }
    }
    public void sendChunk() {
        Socket[] sockets = new Socket[SERVER_PORTS.length];
        try {
            // 申请三个连接
            for (int i = 0; i < sockets.length; ++i) {
                sockets[i] = new Socket(SERVER_HOST, SERVER_PORTS[i]);
                OutputStream outputStream = sockets[i].getOutputStream();
                outputStream.write(request.getBytes(StandardCharsets.UTF_8));
            }
            // 传输块文件
            for (int i = 0; i < chunkCount; ++i) {
                Random random = new Random();
                int randomPortIndex = random.nextInt(sockets.length);
                // 映射表记录
                if (hash.containsKey(SERVER_PORTS[randomPortIndex])) {
                    List<Integer> v = hash.get(SERVER_PORTS[randomPortIndex]);
                    v.add(i);
                    hash.put(SERVER_PORTS[randomPortIndex], v);
                } else {
                    hash.put(SERVER_PORTS[randomPortIndex], new ArrayList<>(Arrays.asList(i)));
                }
                // 文件名
                String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + i + "." + fileExt;
                // 打开chunk文件
                try (FileChannel fileChannel = FileChannel.open(Paths.get(chunkFilePath), StandardOpenOption.CREATE)) {
                    DataOutputStream dataOutputStream = new DataOutputStream(sockets[randomPortIndex].getOutputStream());
                    SendFile sendFile = new SendFile(fileName + i + "." + fileExt, fileChannel, dataOutputStream);
                    sendFile.send();

//                    System.out.println("发送文件成功" + chunkFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("当前映射表记录: " + hash);
            // 哈希表写入文件
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    Files.newOutputStream(Path.of(hashTableFilePath), StandardOpenOption.CREATE)
            )) {
                objectOutputStream.writeObject(hash);
//                System.out.println("哈希表写入成功");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 关闭连接
            for (int i = 0; i < sockets.length; ++i) {
                if (sockets[i] != null && !sockets[i].isClosed()) {
                    try {
                        sockets[i].close();
                    } catch (IOException e) {
                        System.err.println("无法关闭连接: " + e.getMessage());
                    }
                }
            }
        }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void deleteChunkFile() {
        // 循环读取块的过程放入一个线程池中
        ExecutorService executorService = Executors.newFixedThreadPool(chunkCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < chunkCount; ++i) {
            int chunkIndex = i;
            // 封装为异步任务交给线程池处理
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // 删除临时chunk文件
                String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + chunkIndex + "." + fileExt;
                File chunkFile = new File(chunkFilePath);
                if (chunkFile.exists()) {
                    chunkFile.delete();
                }
            }, executorService);
            futures.add(future);
        }
        // 等待所有任务完成关闭线程池
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
    }

    public static void UploadFile(String filePath) {
        Upload upload = new Upload(filePath);
        upload.getChunk();
        upload.sendChunk();
        upload.deleteChunkFile();
    }

    public static void main(String[] args) {
        Upload upload = new Upload("./test.pdf");
        upload.getChunk();
        upload.sendChunk();
        upload.deleteChunkFile();
    }
}

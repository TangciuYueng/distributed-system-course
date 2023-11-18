package cn.edu.tongji.client;

import cn.edu.tongji.util.SendFile;

import java.io.*;
import java.net.Socket;
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
    protected String filePath;
    protected String fileName;
    protected static final String BASE_CHUNK_FILE_PATH = "./";
    private static final String request = "U";
    protected static final int CHUNK_SIZE = 1024 * 1024;
    protected int chunkCount;
    protected final String hashTableFilePath;
    protected static String SERVER_HOST = "localhost";
    protected static int[] SERVER_PORTS = {8887, 8888, 8889};
    protected Map<Integer, List<Integer>> hash = new HashMap<>();

    public Upload(String filePath) {
        this.filePath = filePath;
        Path path = Paths.get(filePath);
        this.fileName = path.getFileName().toString();
        this.hashTableFilePath = fileName + ".map";
    }
    protected void getChunk() {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
             FileChannel fileChannel = file.getChannel()) {
            // total number of bytes
            long fileSize = file.length();
            // number of file chunks
            chunkCount = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
            // get the thread pool
            ExecutorService executorService = Executors.newFixedThreadPool(chunkCount);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // get file chunks
            for (int i = 0; i < chunkCount; ++i) {
                int chunkIndex = i;
                // async dealing
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    // test.txt -> test.txt$0/test.txt$1...
                    String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + "$" + chunkIndex;
                    try (RandomAccessFile chunkFile = new RandomAccessFile(chunkFilePath, "rw");
                         FileChannel chunkFileChannel = chunkFile.getChannel()) {
                        long position = (long) chunkIndex * CHUNK_SIZE;
                        long remaining = fileSize - position;
                        long chunkSize = Math.min(CHUNK_SIZE, remaining);
                        fileChannel.transferTo(position, chunkSize, chunkFileChannel);
                    } catch (Exception e) {
                        System.out.println(chunkFilePath + " error");
                    }
                }, executorService);
                futures.add(future);
            }
            // close the thread pool after waiting all the thread complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            executorService.shutdown();

        } catch (Exception e) {
            System.out.println(filePath + " error");
        }
    }
    protected void deleteChunkFile() {
        // get the thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(chunkCount);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < chunkCount; ++i) {
            int chunkIndex = i;
            // async dealing
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // delete temp file chunks
                Path chunkFilePath = Paths.get(BASE_CHUNK_FILE_PATH + fileName + "$" + chunkIndex);
                if (Files.exists(chunkFilePath)) {
                    try {
                        Files.delete(chunkFilePath);
                    } catch (IOException e) {
                        System.out.println(chunkFilePath + "deleted failed");
                    }
                }
            }, executorService);
            futures.add(future);
        }
        // close the thread pool after waiting all the thread complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
    }
    protected void allocateChunk() {
        for (int i = 0; i < chunkCount; ++i) {
            Random random = new Random();
            int randomPortIndex = random.nextInt(SERVER_PORTS.length);
            if (hash.containsKey(SERVER_PORTS[randomPortIndex])) {
                List<Integer> v = hash.get(SERVER_PORTS[randomPortIndex]);
                v.add(i);
                hash.put(SERVER_PORTS[randomPortIndex], v);
            } else {
                hash.put(SERVER_PORTS[randomPortIndex], new ArrayList<>(List.of(i)));
            }
        }
    }
    protected void sendChunk() {
        Socket[] sockets = new Socket[SERVER_PORTS.length];
        DataOutputStream[] dataOutputStreams = new DataOutputStream[SERVER_PORTS.length];
        try {
            for (int i = 0; i < sockets.length; ++i) {
                // this one is not allocated
                if (!hash.containsKey(SERVER_PORTS[i])) {
                    continue;
                }
                // apply for connection
                sockets[i] = new Socket(SERVER_HOST, SERVER_PORTS[i]);
                dataOutputStreams[i] = new DataOutputStream(sockets[i].getOutputStream());
                // send the request type
                dataOutputStreams[i].write(request.getBytes(StandardCharsets.UTF_8));
                // send file name length
                byte[] fileNameByte = fileName.getBytes(StandardCharsets.UTF_8);
                dataOutputStreams[i].writeInt(fileNameByte.length);
                // send file name
                dataOutputStreams[i].write(fileNameByte);
                // get the allocated chunk of this port
                List<Integer> v = hash.get(SERVER_PORTS[i]);
                // send the number of file chunks
                dataOutputStreams[i].writeInt(v.size());
                // send the file chunks
                for (Integer j: v) {
                    String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + "$" + j;
                    // send file
                    try (FileChannel fileChannel = FileChannel.open(Paths.get(chunkFilePath), StandardOpenOption.READ)) {
                        dataOutputStreams[i] = new DataOutputStream(sockets[i].getOutputStream());
                        SendFile sendFile = new SendFile(fileName + "$" + j, fileChannel, dataOutputStreams[i]);
                        sendFile.send();
                        System.out.println(chunkFilePath + " is uploaded successfully");
                    } catch (IOException e) {
                        System.out.println(filePath + " error");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("send error");
        } finally {
            for (int i = 0; i < sockets.length; ++i) {
                if (sockets[i] != null && !sockets[i].isClosed()) {
                    try {
                        sockets[i].close();
                    } catch (IOException e) {
                        System.out.println(sockets[i] + " close failed");
                    }
                }
                if (dataOutputStreams[i] != null) {
                    try {
                        dataOutputStreams[i].close();
                    } catch (IOException e) {
                        System.out.println(dataOutputStreams[i] + " close failed");
                    }
                }
            }
        }
    }

    protected void saveHashtable() {
        Path path = Path.of(hashTableFilePath);
        if (!Files.exists(path)) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    Files.newOutputStream(path, StandardOpenOption.CREATE)
            )) {
                objectOutputStream.writeObject(hash);
                System.out.println("hash table file saved successfully");
            } catch (IOException e) {
                e.printStackTrace();
            }
//        } else {
//            System.out.println("hash table file exists");
        }
    }

    public static void UploadFile(String filePath) {
        Upload upload = new Upload(filePath);
        upload.getChunk();
        upload.allocateChunk();
        upload.sendChunk();
        upload.deleteChunkFile();
        upload.saveHashtable();
    }

    public static void main(String[] args) {
        Upload upload = new Upload("./test.txt");
        upload.getChunk();
        upload.allocateChunk();
        upload.sendChunk();
        upload.deleteChunkFile();
        upload.saveHashtable();
    }


}

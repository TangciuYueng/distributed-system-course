package cn.edu.tongji.client;

import cn.edu.tongji.util.ReceiveFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CDownload extends Download {
    private static final String request = "G";
    private Map<Integer, List<Integer>> hashRequiredChunk;
    private Map<Integer, List<Integer>> hash;
    private final String hashTableFilePath;
    private final String hashReceivedPath;
    public CDownload(String fileName) {
        super(fileName);
        hashRequiredChunk = new HashMap<>();
        hash = new HashMap<>();
        hashTableFilePath = fileName + ".map";
        hashReceivedPath = fileName + ".received";
    }

    @Override
    protected void getChunk() {
//        System.out.println("require hash" + hashRequiredChunk);
        for (int port: SERVER_PORTS) {
            try (Socket socket = new Socket(SERVER_HOST, port);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
                // sent request type
                dataOutputStream.write(request.getBytes(StandardCharsets.UTF_8));
                // sent requested file name
                byte[] fileNameByte = fileName.getBytes(StandardCharsets.UTF_8);
                dataOutputStream.writeInt(fileNameByte.length);
                dataOutputStream.write(fileNameByte);
                // sent the number of the required file names
                List<Integer> v;
                if (hashRequiredChunk.containsKey(port)) {
                    v = hashRequiredChunk.get(port);
                } else {
                    v = new ArrayList<>();
                }
                dataOutputStream.writeInt(v.size());
                for (int chunkIndex: v) {
                    String chunkFileName = fileName + "$" + chunkIndex;
                    byte[] chunkFileNameByte = chunkFileName.getBytes(StandardCharsets.UTF_8);
                    dataOutputStream.writeInt(chunkFileNameByte.length);
                    dataOutputStream.write(chunkFileNameByte);
                }
                // get file chunks
                for (int i = 0; i < v.size(); ++i) {
                    // get the length of each file name
                    int fileNameLength = dataInputStream.readInt();
                    // get the content of the file
                    ReceiveFile receiveFile = new ReceiveFile(fileNameLength, basePath, dataInputStream);
                    int chunkIndex = receiveFile.receive();
                    // get one save one
                    saveChunkIndex(chunkIndex);
                    // send successful flag
                    dataOutputStream.writeInt(0);
                }
            } catch (Exception e) {
                System.out.println(port + " connected failed");
            }
        }
    }

    private void saveChunkIndex(int chunkIndex) throws IOException {
        Path filePath = Paths.get(hashReceivedPath);
        // check if the file exists
        if (!Files.exists(filePath)) {
            // if the file doesn't exist, create a new file and write the chunkIndex
            String chunkIndexLine = chunkIndex + System.lineSeparator();
            Files.write(filePath, chunkIndexLine.getBytes(), StandardOpenOption.CREATE);
        } else {
            // if the file exists, append the chunkIndex
            String chunkIndexLine = chunkIndex + System.lineSeparator();
            Files.write(filePath, chunkIndexLine.getBytes(), StandardOpenOption.APPEND);
        }
    }

    private void getRequiredChunk() {
        Set<Integer> chunkIndex = getChunkSet(hashReceivedPath);
        for (Map.Entry<Integer, List<Integer>> entry: hash.entrySet()) {
            int port = entry.getKey();
            List<Integer> v = entry.getValue();

            for (Integer index: v) {
                // no received
                if (!chunkIndex.contains(index)) {
                    if (hashRequiredChunk.containsKey(port)) {
                        List<Integer> vTemp = hashRequiredChunk.get(port);
                        vTemp.add(index);
                        hashRequiredChunk.put(port, vTemp);
                    } else {
                        hashRequiredChunk.put(port, new ArrayList<>(List.of(index)));
                    }
                }
            }
        }
        System.out.println("require: " + hashRequiredChunk);
    }

    private Set<Integer> getChunkSet(String fileName) {
        Path filePath = Paths.get(fileName);
        Set<Integer> chunkIndex;
        try {
            chunkIndex = Files.lines(filePath)
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            chunkIndex = new HashSet<>();
        }
        System.out.println("local " + chunkIndex);
        return chunkIndex;
    }

    private void getHashTable() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Path.of(hashTableFilePath), StandardOpenOption.READ)
        )) {
            hash = (HashMap<Integer, List<Integer>>) objectInputStream.readObject();
        } catch (Exception e) {
            System.out.println("no ser file error");
        }
//        System.out.println("hash " + hash);
    }

    @Override
    protected boolean fileExists() {
        // get chunk count
        for (List<Integer> v: hash.values()) {
            chunkCount.addAndGet(v.size());
        }
        for (List<Integer> v: hashRequiredChunk.values()) {
            for (Integer chunkIndex: v) {
                Path chunkFilePath = Paths.get(basePath, fileName + "$" + chunkIndex);
                if (!Files.exists(chunkFilePath)) {
                    return false;
                }
            }
        }
        return chunkCount.get() > 0;
    }

    public static void downloadFile(String fileName) {
        CDownload cDownload = new CDownload(fileName);
        cDownload.getHashTable();
        cDownload.getRequiredChunk();
        cDownload.getChunk();
        if (cDownload.fileExists()) {
            cDownload.mergeFile();
//            cDownload.deleteChunkFile();
        } else {
            System.out.println(fileName + " get failed");
        }
    }
    public static void main(String[] args) {
        CDownload cDownload = new CDownload("test.pdf");
        cDownload.getHashTable();
        cDownload.getRequiredChunk();
        cDownload.getChunk();
        if (cDownload.fileExists()) {
            cDownload.mergeFile();
            cDownload.deleteChunkFile();
        } else {
            System.out.println("test.txt doesn't exists");
        }
    }
}

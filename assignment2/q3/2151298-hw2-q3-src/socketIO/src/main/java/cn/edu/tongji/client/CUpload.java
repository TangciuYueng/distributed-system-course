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

public class CUpload extends Upload {
    private final static String request = "P";
    private Map<Integer, List<Integer>> hashSent;
    private final String hashSentPath;
    public CUpload(String filePath) {
        super(filePath);
        hashSentPath = BASE_CHUNK_FILE_PATH + fileName + ".sent";
        getHashSent();
    }

    private void getHashSent() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Path.of(hashSentPath), StandardOpenOption.READ)
        )) {
            hashSent = (HashMap<Integer, List<Integer>>) objectInputStream.readObject();
        } catch (Exception e) {
            // hash table sent error then make a new one
            System.out.println("get hash sent failed");
            hashSent = new HashMap<>();
        }
    }

    @Override
    protected void sendChunk() {
        Socket[] sockets = new Socket[SERVER_PORTS.length];
        DataOutputStream[] dataOutputStreams = new DataOutputStream[SERVER_PORTS.length];
        DataInputStream[] dataInputStreams = new DataInputStream[SERVER_PORTS.length];
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
                // get sent chunk
                List<Integer> vSent;
                if (hashSent.containsKey(SERVER_PORTS[i])) {
                    vSent = hashSent.get(SERVER_PORTS[i]);
                } else {
                    vSent = new ArrayList<>();
                }
                // send the number of file chunks
                dataOutputStreams[i].writeInt(v.size() - vSent.size());
                // send the file chunks
                for (Integer j: v) {
                    // if chunk j have sent then next one
                    if (hashSent.containsKey(SERVER_PORTS[i]) && hashSent.get(SERVER_PORTS[i]).contains(j)) {
                        continue;
                    }
                    String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + "$" + j;
                    // send file
                    try (FileChannel fileChannel = FileChannel.open(Paths.get(chunkFilePath), StandardOpenOption.READ)) {
                        dataOutputStreams[i] = new DataOutputStream(sockets[i].getOutputStream());
                        dataInputStreams[i] = new DataInputStream(sockets[i].getInputStream());
                        SendFile sendFile = new SendFile(fileName + "$" + j, fileChannel, dataOutputStreams[i]);
                        sendFile.send();
                        // get the successful sending flag
                        sockets[i].setSoTimeout(500);
                        dataInputStreams[i].readInt();
                        System.out.println(chunkFilePath + " is uploaded successfully");
                        // after sending successfully add j into hash table that record sent chunk number
                        if (hashSent.containsKey(SERVER_PORTS[i])) {
                            List<Integer> vTemp = hashSent.get(SERVER_PORTS[i]);
                            vTemp.add(j);
                            hashSent.put(SERVER_PORTS[i], vTemp);
                        } else {
                            hashSent.put(SERVER_PORTS[i], new ArrayList<>(List.of(j)));
                        }
                    } catch (IOException e) {
                        System.out.println(chunkFilePath + " error");
                        break;
                    }
//                    System.out.println("hashSent " + hashSent);
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

    private void getHashTable() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Path.of(hashTableFilePath), StandardOpenOption.READ)
        )) {
            hash = (HashMap<Integer, List<Integer>>) objectInputStream.readObject();
        } catch (Exception e) {
            // hash table error re-allocate
            allocateChunk();
        }
//        System.out.println("hash " + hash);
    }

    private void saveHashSent() {
        Path path = Path.of(hashSentPath);
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        )) {
            objectOutputStream.writeObject(hashSent);
            System.out.println("hash table sent saved successfully");
        } catch (IOException e) {
            System.out.println("hash table sent saved failed");
        }
    }

    public static void uploadFile(String filePath) {
        CUpload cUpload = new CUpload(filePath);
        cUpload.getChunk();
        cUpload.getHashTable();
        cUpload.sendChunk();
        cUpload.deleteChunkFile();
        cUpload.saveHashtable();
        cUpload.saveHashSent();
    }

    public static void main(String[] args) {
        CUpload cUpload = new CUpload("./test.pdf");
        cUpload.getChunk();
        cUpload.getHashTable();
        cUpload.sendChunk();
        cUpload.deleteChunkFile();
        cUpload.saveHashtable();
        cUpload.saveHashSent();
    }
}

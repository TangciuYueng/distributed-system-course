package cn.edu.tongji.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CheckStatus {
    private static String SERVER_HOST = "localhost";
    private static int[] SERVER_PORTS = {8887, 8888, 8889};
    private final String hashTableFilePath;
    private final String fileName;
    private final static String request = "C";
    private HashMap<Integer, List<Integer>> hash;
    private HashMap<Integer, List<Integer>> hashRemote;
    public CheckStatus(String fileName) {
        this.fileName = fileName;
        this.hashTableFilePath = fileName + ".map";
        this.hash = new HashMap<>();
        this.hashRemote = new HashMap<>();
    }
    private void checkServer() {
        for (int port: SERVER_PORTS) {
            // init the value of hash remote to record remote info
            List<Integer> v = new ArrayList<>();
            try (Socket socket = new Socket(SERVER_HOST, port);
                 DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                 DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
                System.out.println(SERVER_HOST + ":" + port + " is on");
                // send request type
                dataOutputStream.write(request.getBytes(StandardCharsets.UTF_8));
                // send the length of file name
                byte[] fileNameByte = fileName.getBytes(StandardCharsets.UTF_8);
                dataOutputStream.writeInt(fileNameByte.length);
                dataOutputStream.write(fileNameByte);
                // get the file chunk length
                int chunkCount = dataInputStream.readInt();
                // get the file chunk index
                for (int i = 0; i < chunkCount; ++i) {
                    v.add(dataInputStream.readInt());
                }
                // add v into the hash remote
                hashRemote.put(port, v);
            } catch (IOException e){
                System.out.println(SERVER_HOST + ":" + port + " is down");
            }
        }
        // print remote info
        printRemoteInfo(hashRemote);
    }
    private void checkFile() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Path.of(hashTableFilePath), StandardOpenOption.READ)
        )) {
            hash = (HashMap<Integer, List<Integer>>) objectInputStream.readObject();
            // there is a map file then print file chunk info
            printLocalInfo(hash);
        } catch (IOException e) {
            System.out.println("local map file doesn't exist");
        } catch (ClassNotFoundException e) {
            System.out.println("local map file has been destroyed");
        }
    }

    private void printRemoteInfo(Map<Integer, List<Integer>> map) {
        boolean allEmpty = true;
        for (List<Integer> v: map.values()) {
            if (v.size() > 0) {
                allEmpty = false;
                break;
            }
        }
        if (allEmpty) {
            System.out.println("no remote info");
        } else {
            System.out.println("Remote info");
            printFileInfo(map);
        }
    }
    private void printLocalInfo(Map<Integer, List<Integer>> map) {
        System.out.println("Local info");
        printFileInfo(map);
    }

    private void printFileInfo(Map<Integer, List<Integer>> map) {
        for (Map.Entry<Integer, List<Integer>> entry: map.entrySet()) {
            Integer port = entry.getKey();
            List<Integer> v = entry.getValue();
            System.out.println(SERVER_HOST + ":" + port);
            System.out.println("number of the chunk files: " + v.size());
            Collections.sort(v);
            System.out.println("sorted ID of chunk files: " + v);
        }
    }

    public void check() {
        checkServer();
        checkFile();
    }
}

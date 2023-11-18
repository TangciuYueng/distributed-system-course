package cn.edu.tongji.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckStatus {
    private static String SERVER_HOST = "localhost";
    private static int[] SERVER_PORTS = {8887, 8888, 8889};
    private final String hashTableFilePath;
    private HashMap<Integer, List<Integer>> hash;
    public CheckStatus(String fileName) {
        this.hashTableFilePath = fileName + ".map";
        this.hash = new HashMap<>();
    }
    private void checkServer() {
        for (int port: SERVER_PORTS) {
            try (Socket socket = new Socket(SERVER_HOST, port)) {
                System.out.println(SERVER_HOST + ":" + port + "is on");
            } catch (IOException e){
                System.out.println(SERVER_HOST + ":" + port + "is down");
            }
        }
    }
    private void checkFile() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Path.of(hashTableFilePath), StandardOpenOption.READ)
        )) {
            hash = (HashMap<Integer, List<Integer>>) objectInputStream.readObject();
            // 调用打印文件信息
            printFileInfo();
        } catch (IOException e) {
            System.out.println("map file doesn't exist");
        } catch (ClassNotFoundException e) {
            System.out.println("map file has been destroyed");
        }
    }

    private void printFileInfo() {
        System.out.println("chunk files in servers");
        for (Map.Entry<Integer, List<Integer>> entry: hash.entrySet()) {
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

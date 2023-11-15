package org.cn.edu.tongji.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class CheckStatus {
    private static String SERVER_HOST = "localhost";
    private static int[] SERVER_PORTS = {8887, 8888, 8889};
    private final String hashTableFilePath;
    private HashMap<Integer, List<Integer>> hash;
    public CheckStatus(String fileName) {
        this.hashTableFilePath = fileName + ".ser";
        this.hash = new HashMap<>();
    }

    private void checkServer() {
        for (int port: SERVER_PORTS) {
            try (Socket socket = new Socket(SERVER_HOST, port)) {
                System.out.println(SERVER_HOST + ":" + port + "运行正常");
            } catch (IOException e){
                System.out.println(SERVER_HOST + ":" + port + "运行异常");
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
            System.out.println("文件不存在");
        } catch (ClassNotFoundException e) {
            System.out.println("文件已损坏");
        }
    }
    private void printFileInfo() {
        System.out.println("文件数据块所在服务器");
        for (Map.Entry<Integer, List<Integer>> entry: hash.entrySet()) {
            Integer port = entry.getKey();
            List<Integer> v = entry.getValue();
            System.out.println(SERVER_HOST + ":" + port);
            System.out.println("包含数据块个数: " + v.size());
            Collections.sort(v);
            System.out.println("排序后的数据块ID: " + v);
        }

    }

    public void check() {
        checkServer();
        checkFile();
    }
}

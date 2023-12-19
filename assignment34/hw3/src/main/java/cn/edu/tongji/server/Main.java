package cn.edu.tongji.server;

import cn.edu.tongji.tools.PersistentBTree;
import cn.edu.tongji.tools.Query;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Main {
     private static final String FILE_NAME = "dblp_line_processed_chunk_1";
    private static final int NUM_BUCKETS = 4;

    public static void main(String[] args) {
        // 在Server端加载trees
        PersistentBTree<String, Long>[] trees = loadTrees();

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server listening on port 8080...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // 每次接收到客户端请求，创建一个新的处理线程
                new ServerHandler(clientSocket, trees).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static PersistentBTree<String, Long>[] loadTrees() {
        PersistentBTree<String, Long>[] trees = new PersistentBTree[NUM_BUCKETS];
        try {
            for (int i = 0; i < NUM_BUCKETS; ++i) {
                trees[i] = PersistentBTree.loadFromFile(FILE_NAME + "_bucket_" + i + "_index_tree.ser");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return trees;
    }
}

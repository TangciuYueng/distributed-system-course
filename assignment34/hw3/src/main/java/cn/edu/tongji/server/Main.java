package cn.edu.tongji.server;

import cn.edu.tongji.tools.PersistentBTree;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static final int SERVER_NUM = 6;
    public static final int BUCKET_PER_SERVER = 4;
    public static final int PORT_BASE = 8080;

    public static void createServerThread(final int serverNum) {
        try (ServerSocket serverSocket = new ServerSocket(PORT_BASE + serverNum)) {
            PersistentBTree<String, Long>[] trees = new PersistentBTree[4];

            for (int i = 0; i < BUCKET_PER_SERVER; i++) {
                final long startTime = System.currentTimeMillis();
                trees[i] = PersistentBTree.loadFromFile("dblp_line_processed_chunk_" + (serverNum + 1) + "_bucket_" + i + "_index_tree.ser");
                System.out.println("服务器" + (serverNum + 1) + "读取" + i + "号桶索引用时：" + ((double) (System.currentTimeMillis() - startTime) / 1000) + "s");
            }

            while (true) {
                System.out.println("Server " + (serverNum + 1) + " Waiting on Port " + (PORT_BASE + serverNum) + "...");    //阻塞等待连接
                Socket connectionSocket = serverSocket.accept();  //阻塞等待连接

                System.out.println("Welcome Connection From " + connectionSocket.getInetAddress());  //连接成功
                DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
                final String author = inFromClient.readUTF();  //读取作者名
                new DataSearchThread(connectionSocket, author, trees).run();  //创建服务对象并运行主过程

                System.out.println(connectionSocket.getInetAddress() + " disconnected");  //断开连接，重新等待
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try (ExecutorService exec = Executors.newCachedThreadPool()) {
            for (int i = 0; i < SERVER_NUM; i++) {
                final int serverNum = i;

                System.out.println("服务器初始化中...");
                exec.execute(() -> createServerThread(serverNum));
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return trees;
    }
}

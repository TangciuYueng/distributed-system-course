package cn.edu.tongji.server;

import cn.edu.tongji.tools.PersistentBTree;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    public static final int COPY_NUM = 4;
    public static final int BUCKET_PER_SERVER = 4;
    public static final int PORT = 9999;

    public static void createServerThread(int serverNum, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            PersistentBTree<String, Long>[] trees = new PersistentBTree[BUCKET_PER_SERVER];

            for (int i = 0; i < BUCKET_PER_SERVER; i++) {
                final long startTime = System.currentTimeMillis();
                trees[i] = PersistentBTree.loadFromFile("dblp_line_processed_chunk_" + (serverNum + 1) + "_bucket_" + i + "_index_tree.ser");
                System.out.println("块" + (serverNum + 1) + "读取" + i + "号桶索引用时：" + ((double) (System.currentTimeMillis() - startTime) / 1000) + "s");
            }

            while (true) {
                System.out.println("Server " + (serverNum + 1) + " Waiting on Port " + port + "...");    //阻塞等待连接
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
        if (args.length != 1) {
            System.out.println("need server num");
            return;
        }
        // 一个服务器 4 个文件块 开 4 个线程 负责 9999 - 10002 端口进行处理
        try {
            int serverNum = Integer.parseInt(args[0]);
            try (ExecutorService exec = Executors.newCachedThreadPool()) {
                for (int i = 0; i < COPY_NUM; ++i) {
                    int finalI = i;
                    int port = PORT + i;
                    exec.execute(() -> {
                        createServerThread((serverNum + finalI) % COPY_NUM, port);
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

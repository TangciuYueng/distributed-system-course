package cn.edu.tongji.server;

import cn.edu.tongji.tools.PersistentBTree;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer {
    private static final String FILE_PREFIX = "dblp_line_processed_chunk_";
    private static final String FILE_POSTFIX = "_index_tree.ser";
    private static final String EXT = ".lson";
    private static final int BUCKET_PER_SERVER = 4;
    private static final int PORT = 9999;

    public static void createServerThread(int chunkNum, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            PersistentBTree<String, Long>[] trees = new PersistentBTree[BUCKET_PER_SERVER];
            List<RandomAccessFile> files = new ArrayList<>();

            // 前置工作 读取索引
            for (int i = 0; i < BUCKET_PER_SERVER; i++) {
                final long startTime = System.currentTimeMillis();
                trees[i] = PersistentBTree.loadFromFile(FILE_PREFIX + (chunkNum + 1) + "_bucket_" + i + FILE_POSTFIX);
                System.out.println("块" + (chunkNum + 1) + "读取" + i + "号桶索引用时：" + ((double) (System.currentTimeMillis() - startTime) / 1000) + "s");
                RandomAccessFile randomAccessFile = new RandomAccessFile(FILE_PREFIX + (chunkNum + 1) + "_bucket_" + i + EXT, "r");
                files.add(randomAccessFile);
            }

            while (true) {
                System.out.println("Server " + " Waiting on Port " + port + "...");    //阻塞等待连接
                try {
                    Socket connectionSocket = serverSocket.accept();  //阻塞等待连接
                    System.out.println("Welcome Connection From " + connectionSocket.getInetAddress());  //连接成功
                    try (DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream())) {
                        final String author = inFromClient.readUTF();  //读取作者名
                        new DataSearchThread(connectionSocket, author, trees, files, BUCKET_PER_SERVER).run();  //创建服务对象并运行主过程
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(connectionSocket.getInetAddress() + " disconnected");  //断开连接，重新等待
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if ((args.length != 4) && (args.length != 5)) {
            System.out.println("need args: <copyNum1> <copyNum2> ...");
            return;
        }
        // 一个服务器 args.length 个文件块 开 args.length 个线程 负责 9999 - 10002/3 端口进行处理
        try {
            try (ExecutorService exec = Executors.newFixedThreadPool(args.length)) {
                for (int i = 0; i < args.length; ++i) {
                    int chunkNum = Integer.parseInt(args[i]);
                    int port = PORT + i;
                    exec.execute(() -> {
                        createServerThread(chunkNum, port);
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
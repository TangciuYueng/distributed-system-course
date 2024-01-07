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
            List<BufferedReader> bufferReaders = new ArrayList<>();

            // 前置工作 读取索引
            for (int i = 0; i < BUCKET_PER_SERVER; i++) {
                final long startTime = System.currentTimeMillis();
                trees[i] = PersistentBTree.loadFromFile( FILE_PREFIX + (chunkNum + 1) + "_bucket_" + i + FILE_POSTFIX);
                System.out.println("块" + (chunkNum + 1) + "读取" + i + "号桶索引用时：" + ((double) (System.currentTimeMillis() - startTime) / 1000) + "s");

                BufferedReader inFromLson = new BufferedReader(new FileReader(FILE_PREFIX + (chunkNum + 1) + "_bucket_" + i + EXT));
                // 标记初始位置 方便后面 skip 后的复原
                inFromLson.mark(0);
                bufferReaders.add(inFromLson);
            }

            while (true) {
                System.out.println("Server " + " Waiting on Port " + port + "...");    //阻塞等待连接
                Socket connectionSocket = serverSocket.accept();  //阻塞等待连接

                System.out.println("Welcome Connection From " + connectionSocket.getInetAddress());  //连接成功
                DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
                final String author = inFromClient.readUTF();  //读取作者名
                new DataSearchThread(connectionSocket, author, trees, bufferReaders, BUCKET_PER_SERVER).run();  //创建服务对象并运行主过程

                System.out.println(connectionSocket.getInetAddress() + " disconnected");  //断开连接，重新等待
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("need args: <startChunkNum> <copyNum>");
            return;
        }
        // 一个服务器 copyNum 个文件块 开 copyNum 个线程 负责 9999 - 10002/3 端口进行处理
        try {
            int startChunkNum = Integer.parseInt(args[0]);
            int copyNum = Integer.parseInt(args[1]);
            try (ExecutorService exec = Executors.newFixedThreadPool(copyNum)) {
                for (int i = 0; i < copyNum; ++i) {
                    int finalI = i;
                    int port = PORT + i;
                    exec.execute(() -> {
                        createServerThread((startChunkNum + finalI) % copyNum, port);
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

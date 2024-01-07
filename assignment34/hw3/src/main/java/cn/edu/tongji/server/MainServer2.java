package cn.edu.tongji.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServer2 {
    private static final String FILE_PREFIX = "dblp_line_processed_chunk_";
    private static final String EXT = ".lson";
    private static final int PORT = 9999;

    public static void createServerThread(int chunkNum, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                System.out.println("Server " + " Waiting on Port " + port + "...");    //阻塞等待连接
                try {
                    Socket connectionSocket = serverSocket.accept();  //阻塞等待连接
                    System.out.println("Welcome Connection From " + connectionSocket.getInetAddress());  //连接成功
                    try (DataInputStream inFromClient = new DataInputStream(connectionSocket.getInputStream());
                        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream())) {
                        final String author = inFromClient.readUTF();  //读取作者名
                        int hash = customHashFunction(author);
                        String script = "grep " + author + " " + FILE_PREFIX + (chunkNum + 1) + "_bucket_" + hash + EXT;
                        String scriptFilePath = "inline_script.sh";
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFilePath))) {
                            writer.write(script);
                        }
                        // 设置Shell脚本文件的执行权限
                        ProcessBuilder chmodProcess = new ProcessBuilder("chmod", "+x", scriptFilePath);
                        Process chmod = chmodProcess.start();
                        int chmodExitCode = chmod.waitFor();
                        if (chmodExitCode != 0) {
                            System.err.println("Failed to set execute permission for the script.");
                            System.exit(chmodExitCode);
                        }

                        // 执行Shell脚本
                        ProcessBuilder processBuilder = new ProcessBuilder(scriptFilePath);
                        processBuilder.redirectErrorStream(true); // 合并标准错误输出到标准输出流
                        Process process = processBuilder.start();

                        // 获取脚本执行结果
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println("Script Output: " + line);
                            }

                            // 找到信息
                            outToClient.writeBoolean(true);
                            // 发送字符串长度与字节数组
                            byte[] dataBytes = line.getBytes(StandardCharsets.UTF_8);
                            outToClient.writeInt(dataBytes.length);
                            outToClient.write(dataBytes);
                        }

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

    private static int customHashFunction(String name) {
        // 初始化哈希值
        int hashValue = 0;

        // 遍历姓名中的每个字符
        for (char ch : name.toCharArray()) {
            // 将字符的 Unicode 码值加到哈希值中
            hashValue += ch;
        }

        return hashValue;
    }
}

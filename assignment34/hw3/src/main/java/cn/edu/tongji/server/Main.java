package cn.edu.tongji.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try {
            // 创建ServerSocket并监听指定端口
            ServerSocket serverSocket = new ServerSocket(8080);

            System.out.println("Server listening on port 8080...");

            // 服务器持续监听客户端连接
            while (true) {
                // 当有客户端连接时，accept方法返回一个Socket对象
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // 为每个客户端启动一个新的线程进行通信
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 定义一个简单的客户端处理类
    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // 获取输入流
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // 读取客户端发送的数据
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Received from client: " + message);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    // 关闭Socket连接
                    clientSocket.close();
                    System.out.println("Client disconnected: " + clientSocket.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

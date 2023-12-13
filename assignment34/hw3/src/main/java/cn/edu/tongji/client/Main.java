package cn.edu.tongji.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        try{
            // 创建Socket并连接到服务器的指定端口
            Socket socket = new Socket("localhost", 8080);

            // 获取输出流
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // 发送消息到服务器
            writer.println("Hello, Server!");

            // 关闭Socket连接
            socket.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}

package cn.edu.tongji.component;

import cn.edu.tongji.config.Config;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class QueryService implements Runnable {
    @Override
    public void run() {
        try {
            System.out.println("[QueryService]: query service boot");
            try (ServerSocket serverSocket = new ServerSocket(Config.queryPort)) {
                Logger queryLogger = new Logger("??");

                // 建立连接并等待客户端 socket
                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    try (DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                         DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream())) {

                        String queryInfo = dataInputStream.readUTF();
                        String queryResult = queryLogger.query(queryInfo);
                        dataOutputStream.writeUTF(queryResult);
                    } catch (IOException e) {
                        System.out.println("[error]: failed to connect");
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("[error]: failed to boot");
            e.printStackTrace();
        }
    }
}

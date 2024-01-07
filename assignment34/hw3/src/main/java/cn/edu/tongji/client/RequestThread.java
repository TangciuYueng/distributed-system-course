package cn.edu.tongji.client;

import cn.edu.tongji.tools.SearchResult;
import lombok.AllArgsConstructor;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@AllArgsConstructor
public class RequestThread implements Callable<SearchResult> {
    private static final int SOCKET_TIMEOUT = 1500;

    private final int chunkNum;
    private final String author;
    private final String address;
    private final int port;

    @Override
    public SearchResult call() {
        try (Socket connectionSocket = new Socket(address, port);
             DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
             DataInputStream inFromServer = new DataInputStream(connectionSocket.getInputStream())
             ) {
            // 设置 timeout
            connectionSocket.setSoTimeout(SOCKET_TIMEOUT);

            // 开始计时
            final long startTime = System.nanoTime();

            // 发送请求的姓名
            outToServer.writeUTF(author);
            // 接收布尔值表示找没找到
            if (!inFromServer.readBoolean()) {
                return new SearchResult(false, null, null);
            }

            // 接收字符串长度
            int length = inFromServer.readInt();
            byte[] dataBytes = new byte[length];
            // 接收数据
            inFromServer.readFully(dataBytes);
            String data = new String(dataBytes, StandardCharsets.UTF_8);
            double time = (double)(System.nanoTime() - startTime) / 1000000000;
            return new SearchResult(true, time, data);
        } catch (SocketTimeoutException e) {
            System.out.println("服务器" + (chunkNum + 1) + "超时...");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return new SearchResult(false, null, null);
    }
}

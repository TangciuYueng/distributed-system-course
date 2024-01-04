package cn.edu.tongji.client;

import lombok.AllArgsConstructor;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

@AllArgsConstructor
public class RequestThread implements Runnable {
    private static final int SOCKET_TIMEOUT = 1500;
    private static final String FILE_PREFIX = "dblp_line_processed_chunk_";
    private static final String EXT = ".lson";

    private final int chunkNum;
    private final String author;
    private final String address;
    private final int port;

    @Override
    public void run() {
        try (Socket connectionSocket = new Socket(address, port)) {
            // 设置 timeout
            connectionSocket.setSoTimeout(SOCKET_TIMEOUT);

            final long startTime = System.nanoTime();
            DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
            DataInputStream inFromServer = new DataInputStream(connectionSocket.getInputStream());
            // 发送请求的姓名
            outToServer.writeUTF(author);
            // 接收布尔值表示找没找到
            if (!inFromServer.readBoolean()) {
                System.out.println("服务器" + (chunkNum + 1) + "未找到结果");
                return;
            }

            final int bucketNum = inFromServer.readInt();
            final long pointer = inFromServer.readLong();
            System.out.println("服务器" + (chunkNum + 1) + "，桶" + bucketNum + "找到结果");
            BufferedReader inFromLson = new BufferedReader(new FileReader(FILE_PREFIX + (chunkNum + 1) + "_bucket_" + bucketNum + EXT));
            inFromLson.skip(pointer);
            String jsonData = inFromLson.readLine();
            System.out.println(jsonData);
            System.out.println("本次查询：" + ((double) (System.nanoTime() - startTime) / 1000000000) + "s");

            inFromLson.close();
        } catch (SocketTimeoutException e) {
            System.out.println("服务器" + (chunkNum + 1) + "连接超时...");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

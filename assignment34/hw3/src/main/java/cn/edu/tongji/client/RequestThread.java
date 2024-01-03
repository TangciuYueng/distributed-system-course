package cn.edu.tongji.client;

import lombok.AllArgsConstructor;

import java.io.*;
import java.net.Socket;
import static cn.edu.tongji.server.MainServer.PORT;

@AllArgsConstructor
public class RequestThread implements Runnable {
    private final int serverNum;
    private final String author;
    private final String address;
    private final int port;

    @Override
    public void run() {
        try (Socket connectionSocket = new Socket(address, port)) {
            final long startTime = System.nanoTime();
            DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
            DataInputStream inFromServer = new DataInputStream(connectionSocket.getInputStream());

            outToServer.writeUTF(author);

            if (!inFromServer.readBoolean()) {
                System.out.println("服务器" + (serverNum + 1) + "未找到结果");
                return;
            }

            final int bucketNum = inFromServer.readInt();
            final long pointer = inFromServer.readLong();
            System.out.println("服务器" + (serverNum + 1) + "，桶" + bucketNum + "找到结果");
            BufferedReader inFromLson = new BufferedReader(new FileReader("dblp_line_processed_chunk_" + (serverNum + 1) + "_bucket_" + bucketNum + ".lson"));
            inFromLson.skip(pointer);
            String jsonData = inFromLson.readLine();
            System.out.println(jsonData);
            System.out.println("本次查询：" + ((double) (System.nanoTime() - startTime) / 1000000000) + "s");

            inFromLson.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package cn.edu.tongji.server;

import cn.edu.tongji.tools.PersistentBTree;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ServerHandler extends Thread {
    private Socket clientSocket;
    private PersistentBTree<String, Long>[] trees;

    public ServerHandler(Socket clientSocket, PersistentBTree<String, Long>[] trees) {
        this.clientSocket = clientSocket;
        this.trees = trees;
    }

    @Override
    public void run() {
        try (
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            // 从客户端接收姓名数据
            int length = dataInputStream.readInt();
            byte[] nameBytes = new byte[length];
            dataInputStream.readFully(nameBytes);
            String name = new String(nameBytes, StandardCharsets.UTF_8);

            // 在Server端进行数据查询
            int hashResult = customHashFunction(name) % trees.length;
            Long pointer = trees[hashResult].search(name);

            if (pointer != null) {
                try (BufferedReader reader = new BufferedReader(new FileReader("dblp_line_processed_chunk_1_bucket_" + hashResult + ".lson"))) {
                    reader.skip(pointer);
                    String jsonData = reader.readLine();
                    // 将查询结果发送给客户端
                    dataOutputStream.writeInt(jsonData.length());
                    dataOutputStream.write(jsonData.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                    dataOutputStream.writeInt(0); // 发生异常时发送长度为0表示查询失败
                }
            } else {
                dataOutputStream.writeInt(0); // 如果pointer为null，发送长度为0表示查询失败
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static int customHashFunction(String name) {
        int hashValue = 0;
        for (char ch : name.toCharArray()) {
            hashValue += (int) ch;
        }
        return hashValue;
    }
}

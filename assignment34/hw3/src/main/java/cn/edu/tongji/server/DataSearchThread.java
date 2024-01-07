package cn.edu.tongji.server;

import cn.edu.tongji.tools.PersistentBTree;
import lombok.AllArgsConstructor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

@AllArgsConstructor
public class DataSearchThread implements Runnable {
    private final Socket connectionSocket;
    private final String author;
    private final PersistentBTree<String, Long>[] trees;
    private final List<RandomAccessFile> bufferedReaders;
    private final int bucketPerChunk;

    @Override
    public void run() {
        try (DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream())) {
            final int bucketNum = customHashFunction(author) % bucketPerChunk;
            // 找到文件指针
            Long pointer = trees[bucketNum].search(author);

            // 找不到
            if (pointer == null) {
                outToClient.writeBoolean(false);
                return;
            }

            RandomAccessFile reader = bufferedReaders.get(bucketNum);

            reader.seek(pointer);
            String data = reader.readLine();

            // 找到信息
            outToClient.writeBoolean(true);
            // 发送字符串长度与字节数组
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            outToClient.writeInt(dataBytes.length);
            outToClient.write(dataBytes);

            outToClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int customHashFunction(String name) {
        // 初始化哈希值
        int hashValue = 0;

        // 遍历姓名中的每个字符
        for (char ch : name.toCharArray()) {
            // 将字符的Unicode码值加到哈希值中
            hashValue += ch;
        }

        return hashValue;
    }
}

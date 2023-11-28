package org.kricktYueng.channel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class TestFIleChannel {
    public static void main(String[] args) {
        // FileChannel 只能工作在阻塞模式下
        // 怎么获取的 channel 就决定了其读写能力

        // 不能说 buffer 里面有多少数据 channel 就能 write 多少
        // 因此最好利用 hasRemaining 判断
        ByteBuffer buffer = StandardCharsets.UTF_8.encode("Hola, mis amigos.");
        try (FileChannel channel = new RandomAccessFile("espanol.txt", "rw").getChannel()) {
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            // 获取当前位置
            System.out.println(channel.position());
            // 文件大小
            System.out.println(channel.size());
            // 强制将数据刷新到磁盘上的存储设备
            channel.force(true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

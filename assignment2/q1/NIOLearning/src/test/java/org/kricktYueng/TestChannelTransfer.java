package org.kricktYueng;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class TestChannelTransfer {
    public static void main(String[] args) {
        try (
                FileChannel from = new RandomAccessFile("words.txt", "rw").getChannel();
                FileChannel to = new RandomAccessFile("words2.txt", "rw").getChannel()
                ) {
            // 零拷贝 效率高
            // 但一次最多传输 2G 因此我们也要通过 while
            // 追加到末尾
            long size = from.size();
            for (long left = size; left > 0; ) {
                to.position(to.size());
                // 返回本次写入字节数量
                left -= from.transferTo((size - left), left, to);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package org.kricktYueng;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class TestBuffer1 {
    public static void main(String[] args) {
        // 注意 FileChannel 是阻塞的
        // 获取的 stream 的类型决定了 channel 的读写能力
        try (FileChannel fileChannel = new FileInputStream("test.txt").getChannel()) {
            // 分配10 byte 作为缓冲区
            // 堆内存、读写效率较低，收到 GC(垃圾回收) 的影响
            ByteBuffer buffer = ByteBuffer.allocate(10);
            // 直接内存、读写效率高 少拷贝一次 不会收到 GC 影响 但分配内存比较慢
//            ByteBuffer buffer1 = ByteBuffer.allocateDirect(10);
            while (true) {
                // 从 channel 中读取
                // 写模式 limit 和 capacity 一样
                int len = fileChannel.read(buffer);
                log.debug("读取到的字节数 {}", len);
                // 读到了文件结尾
                if (len == -1) {
                    break;
                }
                // 切换读模式
                buffer.flip();
                // 读模式 limit 为写入的字节数量 capacity 不变
                while (buffer.hasRemaining()) {
                    // 获取一个字节
                    byte b = buffer.get();
                    log.debug("实际字节 {}", (char) b);
                }
                // 切换写模式
                buffer.clear();
            }
            // 如果使用 compact 将未读完的向前压缩 读过的就没有咯 切换为写模式
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

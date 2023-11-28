package org.kricktYueng.buffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TestBufferString {
    public static void main(String[] args) {
        // String 转 ByteBuffer
        String s = "hello";
        byte[] sBytes = s.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(sBytes.length);
        buffer.put(sBytes);
        buffer.flip();
        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }

        // wrap 使用注意之后就是读模式了
        ByteBuffer buffer1 = ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));
        while (buffer1.hasRemaining()) {
            System.out.println((char) buffer1.get());
        }

        // Charset 也是直接切换到读模式了
        ByteBuffer buffer2 = StandardCharsets.UTF_8.encode(s);
        while (buffer2.hasRemaining()) {
            System.out.println((char) buffer2.get());
        }
        // Charset 解码为字符串 注意此前需要设置 buffer 的 position
        buffer2.rewind();
        String s1 = String.valueOf(StandardCharsets.UTF_8.decode(buffer2));
        System.out.println(s1);
    }
}

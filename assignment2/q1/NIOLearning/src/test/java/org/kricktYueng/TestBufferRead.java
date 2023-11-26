package org.kricktYueng;

import java.nio.ByteBuffer;

public class TestBufferRead {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put(new byte[]{'a', 'b', 'c', 'd'});
        buffer.flip();

        buffer.get(new byte[4]);
        // 想反复读 buffer.rewind() 相当于设置 position 为0
        buffer.rewind();
        System.out.println((char) buffer.get());

        // mark & reset
        // mark 记录一个 position 位置，reset 将 position 重置到 mark 的位置
        buffer.mark(); // 在索引为1的位置加入标记
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());
        buffer.reset(); // 重置位置
        System.out.println((char) buffer.get());
        System.out.println((char) buffer.get());

        // get(i) 不会改变 position 的位置
        System.out.println((char) buffer.get(3));
    }
}

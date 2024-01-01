package cn.edu.tongji;

import java.nio.ByteBuffer;

public class TestHw4 {
    @org.junit.Test
    public void testByte() {
        byte[] buffer = { 0x1e, 0x2a, 0x3c, 0x4f };
        byte messageType = ByteBuffer.wrap(buffer).get();
        System.out.println(messageType);
    }
}

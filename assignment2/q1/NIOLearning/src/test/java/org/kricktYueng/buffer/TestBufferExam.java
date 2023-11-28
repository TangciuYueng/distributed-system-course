package org.kricktYueng.buffer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class TestBufferExam {
    /*
    网络上有多条数据发送给服务器，数据之间使用 \n 进行分隔
    由于某些原因接受时候出现了问题
    Hello,world\n
    I'm Alias\n
    How are you?\n
    变成了下面两个 ByteBuffer (黏包、半包)
    Hello,world\nI'm Alias\nHo
    w are you?\n
     */
    public static void main(String[] args) {
        ByteBuffer source = ByteBuffer.allocate(32);
        source.put("Hello,world\nI'm Alias\nHo".getBytes(StandardCharsets.UTF_8));
        split(source);
        source.put("w are you?\n".getBytes(StandardCharsets.UTF_8));
        split(source);
    }

    private static void split(ByteBuffer source) {
        source.flip();
        // 找到每个换行符
        for (int i = 0; i < source.limit(); ++i) {
            // 找到一条消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                // 存入新的 ByteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j = 0; j < length; ++j) {
                    // 这样 source 的 position 就跟着移动了
                    target.put(source.get());
                }
                // 打印 target 信息
                target.flip();
                System.out.println(StandardCharsets.UTF_8.decode(target));
            }
        }
        // 没有读的就压缩
        source.compact();
    }
}

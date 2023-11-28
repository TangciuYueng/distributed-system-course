package org.kricktYueng.nioSocket;


import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestNonBlockingServer {
    public static void main(String[] args) throws IOException {
        // 分配缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 建立服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 设置非阻塞
        serverSocketChannel.configureBlocking(false);
        // 绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8080));

        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            // 这时候线程不暂停 会继续往下运行 而是会返回 null
            SocketChannel channel = serverSocketChannel.accept();
            if (channel != null) {
                log.debug("connected... {}", channel);
                channels.add(channel);
            }
            for (SocketChannel channel1: channels) {
                // 非阻塞 如果没有读到数据会返回0
                int read = channel1.read(buffer);
                if (read > 0) {
                    buffer.flip();
                    while (buffer.hasRemaining()) {
                        System.out.println(buffer.get());
                    }
                    buffer.clear();
                    log.debug("after read {}", channel1);
                }
            }
        }
        // 但是这里一直在跑 虽然可以处理各个连接不相互受影响 但是不常用
    }
}

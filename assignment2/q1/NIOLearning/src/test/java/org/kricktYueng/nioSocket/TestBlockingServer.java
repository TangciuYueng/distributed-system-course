package org.kricktYueng.nioSocket;


import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TestBlockingServer {
    public static void main(String[] args) throws IOException {
        // 分配缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 建立服务器
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 绑定监听端口
        serverSocketChannel.bind(new InetSocketAddress(8080));

        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            log.debug("connecting...");
            // 阻塞方法 线程停止运行
            SocketChannel channel = serverSocketChannel.accept();
            channels.add(channel);
            for (SocketChannel channel1: channels) {
                log.debug("before read {}", channel);
                // 也是阻塞方法，线程停止运行
                channel1.read(buffer);
                buffer.flip();
                while (buffer.hasRemaining()) {
                    System.out.println(buffer.get());
                }
                log.debug("after read {}", channel1);
            }

        }
    }
}

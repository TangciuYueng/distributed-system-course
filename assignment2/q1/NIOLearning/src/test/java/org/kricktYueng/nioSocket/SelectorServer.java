package org.kricktYueng.nioSocket;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class SelectorServer {
    public static void main(String[] args) throws IOException {
        // 创建 selector 管理多个 channel
        Selector selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        // 建立 selector 与 channel 的连接
        // selectionKey 可以知道是哪个 channel 和什么事件(accept有连接请求触发, connect客户端连接建立触发, read客户端发来数据，可读, write可写)
        SelectionKey sscKey = serverSocketChannel.register(selector, 0, null);
        // 设置关注的事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);

        serverSocketChannel.bind(new InetSocketAddress(8080));
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            // 没有事件就阻塞 有事件就往下运行 就不会空循环了
            selector.select();

            // 包含所有发生事件的集合
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                // 从 selectedKey 集合中移除 不然下一次循环还存在但是又没东西可以处理
                iter.remove();
                log.debug("key: {}", key);

                // 处理事件 如果不做 默认又是死循环了
                // 不想处理就取消
//                key.cancel();

                // 区分事件类型
                if (key.isAcceptable()) {
                    // 转换后建立连接
                    ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) key.channel();
                    SocketChannel sc = serverSocketChannel1.accept();
                    // 新的连接同样设置非阻塞 注册到 selector
                    sc.configureBlocking(false);
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    // buffer 作为附件一起注册 有多少个 channel 就对应多少个 buffer
                    SelectionKey scKey = sc.register(selector, 0, buffer);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}", sc);
                } else if (key.isReadable()) {
                    // 注意客户端断开也是读事件
                    try {
                        // 处理读数据逻辑
                        SocketChannel channel = (SocketChannel) key.channel();
                        // 获取对应的 buffer
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = channel.read(buffer);
                        // 处理客户端正常断开
                        if (read == -1) {
                            key.cancel();
                        } else {
                            buffer.flip();
                            while (buffer.hasRemaining()) {
                                System.out.println(buffer.get());
                            }
                        }

                        buffer.clear();
                    } catch (IOException e) {
                        // 反注册 客户端异常断开的时候调用
                        key.cancel();
                    }
                }
            }
        }
    }
}

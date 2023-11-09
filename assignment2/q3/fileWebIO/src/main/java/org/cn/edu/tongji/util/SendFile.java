package org.cn.edu.tongji.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SendFile {
    private String fileName;
    private FileChannel fileChannel;
    private SocketChannel socketChannel;

    public SendFile(String fileName, FileChannel fileChannel, SocketChannel socketChannel) {
        this.fileChannel = fileChannel;
        this.socketChannel = socketChannel;
        this.fileName = fileName;
    }

    public void send() throws IOException {
        // 文件名转换成为字节数组
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        // 发送文件名长度
        ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
        fileNameLengthBuffer.putInt(fileNameBytes.length);
        fileNameLengthBuffer.flip();
        socketChannel.write(fileNameLengthBuffer);
        // 发送文件名
        ByteBuffer fileNameBuffer = ByteBuffer.wrap(fileNameBytes);
        socketChannel.write(fileNameBuffer);
        fileNameBuffer.clear();

        // 发送文件内容长度
        ByteBuffer fileLengthBuffer = ByteBuffer.allocate(Long.BYTES);
        fileLengthBuffer.putLong(fileChannel.size());
        fileLengthBuffer.flip();
        socketChannel.write(fileLengthBuffer);
        // 发送文件内容
        fileChannel.transferTo(0, fileChannel.size(), socketChannel);
    }
}

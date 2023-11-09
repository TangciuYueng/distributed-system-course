package org.cn.edu.tongji.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class SendFile {
    private String fileName;
    private FileChannel fileChannel;
    private DataOutputStream dataOutputStream;

    private static final int BUFFER_SIZE = 4096;

    public SendFile(String fileName, FileChannel fileChannel, DataOutputStream dataOutputStream) {
        this.fileName = fileName;
        this.fileChannel = fileChannel;
        this.dataOutputStream = dataOutputStream;
    }

    public void send() throws IOException {
        // 文件名转换成为字节数组
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        // 发送文件名长度
        dataOutputStream.writeInt(fileNameBytes.length);
        // 发送文件名
        dataOutputStream.write(fileNameBytes);
        // 发送文件内容长度
        dataOutputStream.writeLong(fileChannel.size());
        // 发送文件内容
        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        while (fileChannel.read(byteBuffer) > 0) {
            byteBuffer.flip();
            byte[] data = new byte[byteBuffer.limit()];
            byteBuffer.get(data);
            dataOutputStream.write(data);
            byteBuffer.clear();
        }
    }
}

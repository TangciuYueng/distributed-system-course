package cn.edu.tongji.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class SendFile {

    private final String fileName;
    private final FileChannel fileChannel;
    private final DataOutputStream dataOutputStream;
    private static final int BUFFER_SIZE = 4096;
    public SendFile(String fileName, FileChannel fileChannel, DataOutputStream dataOutputStream) {
        this.fileName = fileName;
        this.fileChannel = fileChannel;
        this. dataOutputStream = dataOutputStream;
    }

    public void send() throws IOException {
        // file name string to byte
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        // send file name length
        dataOutputStream.writeInt(fileNameBytes.length);
        // send file name
        dataOutputStream.write(fileNameBytes);
        // send file content length
        dataOutputStream.writeLong(fileChannel.size());
        // send file content
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

package org.cn.edu.tongji.client;

import org.cn.edu.tongji.util.ReceiveFile;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

public class Download {
    private static final int BUFFER_SIZE = 4 * 1024;
    private String fileName;
    private String hashTableFilePath;
    private int chunkCount;
    private Object[] serverPorts;
    private String fileExt;
    private Hashtable<Integer, Integer> hash;
    private static final String BASE_CHUNK_FILE_PATH = "";
    private static final char request = 'D';
    private static final String SERVER_HOST = "localhost";
    private static final String basePath = "download_files";

    public Download(String fileName) {
        this.fileName = fileName;
        int dotIndex = this.fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            this.fileExt = this.fileName.substring(dotIndex + 1);
            this.fileName = this.fileName.substring(0, dotIndex);
        } else {
            this.fileExt = "";
        }
        this.hashTableFilePath = this.fileName + ".ser";
        hash = null;
    }

    public static void main(String[] args) {
        Download download = new Download("test.txt");
        download.getChunk();
        download.mergeFile();
    }

    private void mergeFile() {
    }

    private void getChunk() {
        // 获得哈希表映射
        getHashTable();
        // 获得有多少块
        chunkCount = hash.size();
        // 建立传输通道
        SocketChannel[] socketChannels = new SocketChannel[serverPorts.length];
        Hashtable<Integer, Integer> chunkToSocketIndex = new Hashtable<>();
        try {
            for (int i = 0; i < socketChannels.length; ++i) {
                socketChannels[i] = SocketChannel.open(new InetSocketAddress(SERVER_HOST, (int) serverPorts[i]));
                // 遍历哈希表的键值对
                Enumeration<Integer> keys = hash.keys();

                while (keys.hasMoreElements()) {
                    Integer key = keys.nextElement();
                    int value = hash.get(key);

                    // 找到与给定值相等的键
                    if (value == (Integer) serverPorts[i]) {
                        // 对应第i个socketChannel
                        chunkToSocketIndex.put(key, i);
                    }
                }
                // 设置非阻塞模式
                socketChannels[i].configureBlocking(false);
                // 发送请求类型
                ByteBuffer requestBuffer = ByteBuffer.allocate(Character.BYTES);
                requestBuffer.putChar(request);
                requestBuffer.flip();
                socketChannels[i].write(requestBuffer);
            }
            // 发送请求需要下载的文件名
            for (int i = 0; i < chunkCount; ++i) {
                // 选择对应的通道
                SocketChannel socketChannel = socketChannels[chunkToSocketIndex.get(i)];
                // 文件名
                String chunkFilePath = BASE_CHUNK_FILE_PATH + fileName + i + "." + fileExt;
                // 文件名转换成字节数组
                byte[] fileNameBytes = chunkFilePath.getBytes(StandardCharsets.UTF_8);
                // 发送文件名长度
                ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
                fileNameLengthBuffer.putInt(fileNameBytes.length);
                fileNameLengthBuffer.flip();
                socketChannel.write(fileNameLengthBuffer);
                // 发送文件名
                ByteBuffer fileNameBuffer = ByteBuffer.wrap(fileNameBytes);
                socketChannel.write(fileNameBuffer);
                fileNameBuffer.clear();

                System.out.println("发送文件名" + chunkFilePath + "成功");
            }
            ByteBuffer test = ByteBuffer.allocate(Integer.BYTES);
            // 接收文件
//            for (SocketChannel socketChannel: socketChannels) {
//                while (true) {
//                    // 接收文件名长度
//                    ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(Integer.BYTES);
//                    if (socketChannel.read(fileNameLengthBuffer) < 0) {
//                        break;
//                    }
//                    fileNameLengthBuffer.flip();
//                    int fileNameLength = fileNameLengthBuffer.getInt();
//                    System.out.println("文件名长度为: " + fileNameLength);
//                    // 得到文件名长度后接收文件并写入磁盘
//                    ReceiveFile receiveFile = new ReceiveFile(socketChannel, fileNameLength, basePath);
//                    receiveFile.receive();
//                }
//            }

            // 关闭连接
            for (int i = 0; i < socketChannels.length; ++i) {
                socketChannels[i].close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int i = 0; i < socketChannels.length; ++i) {
                if (socketChannels[i] != null && socketChannels[i].isOpen()) {
                    try {
                        socketChannels[i].close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void getHashTable() {
        try (ObjectInputStream objectIn = new ObjectInputStream(
                Files.newInputStream(Path.of(hashTableFilePath), StandardOpenOption.READ))) {
            hash = (Hashtable<Integer, Integer>) objectIn.readObject();
            System.out.println("Hashtable已从文件中成功读取并反序列化。");
            HashSet<Integer> uniqueValues = new HashSet<>(hash.values());
            serverPorts = uniqueValues.toArray();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

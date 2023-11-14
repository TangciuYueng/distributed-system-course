package org.cn.edu.tongji.client;

import org.cn.edu.tongji.util.SendFile;

import java.io.*;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;


public class CUpload extends Upload{
    private final static String request = "P";
    public CUpload(String filePath) {
        super(filePath);
    }

    @Override
    public void sendChunk() {
        Socket[] sockets = new Socket[SERVER_PORTS.length];
        DataOutputStream[] dataOutputStreams = new DataOutputStream[SERVER_PORTS.length];
        DataInputStream[] dataInputStreams = new DataInputStream[SERVER_PORTS.length];
        try {
            // 初始化socket 发送断点上传请求标志
            initSocket(sockets, dataOutputStreams);
            // 接收需要上传文件并上传
            getRequiredChunkFileNameAndSend(sockets, dataInputStreams, dataOutputStreams);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接
            for (int i = 0; i < sockets.length; ++i) {
                if (sockets[i] != null && !sockets[i].isClosed()) {
                    try {
                        sockets[i].close();
                    } catch (IOException e) {
                        System.err.println("无法关闭连接: " + e.getMessage());
                    }
                }
                try {
                    if (dataInputStreams[i] != null) {
                        dataInputStreams[i].close();
                    }
                    if (dataOutputStreams[i] != null) {
                        dataOutputStreams[i].close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void getRequiredChunkFileNameAndSend(Socket[] sockets, DataInputStream[] dataInputStreams, DataOutputStream[] dataOutputStreams) throws IOException {
        for (int i = 0; i < sockets.length; ++i) {
            if (!hash.containsKey(SERVER_PORTS[i])) {
                continue;
            }
            dataInputStreams[i] = new DataInputStream(sockets[i].getInputStream());
            DataInputStream dataInputStream = dataInputStreams[i];
            ArrayList<String> requiredChunkFiles = new ArrayList<>();
            // 接收文件名
            int fileNameCount = dataInputStream.readInt();
            for (int j = 0; j < fileNameCount; ++j) {
                int fileNameLength = dataInputStream.readInt();
                byte[] chunkNameByte = new byte[fileNameLength];
                dataInputStream.readFully(chunkNameByte);
                String chunkName = new String(chunkNameByte, StandardCharsets.UTF_8);
                requiredChunkFiles.add(chunkName);
            }

            dataOutputStreams[i] = new DataOutputStream(sockets[i].getOutputStream());
            DataOutputStream dataOutputStream = dataOutputStreams[i];
            // 发送文件
            for (String chunkName: requiredChunkFiles) {
                Path filePath = Paths.get(BASE_CHUNK_FILE_PATH, chunkName);
                try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
                    SendFile sendFile = new SendFile(chunkName, fileChannel, dataOutputStream);
                    sendFile.send();

                    System.out.println("发送文件成功 " + chunkName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            sockets[i].shutdownOutput();
        }
    }
    private void getHashTable() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                Files.newInputStream(Path.of(hashTableFilePath), StandardOpenOption.READ)
        )) {
            hash = (HashMap<Integer, List<Integer>>) objectInputStream.readObject();
        } catch (Exception e) {
            // 读取映射表出问题 重新分配块
            allocateChunk();
        }
    }

    private void saveHashTable() {
        if (!Files.exists(Path.of(hashTableFilePath))) {
            try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(
                    Files.newOutputStream(Path.of(hashTableFilePath), StandardOpenOption.CREATE)
            )) {
                objectOutputStream.writeObject(hash);
                System.out.println("哈希表已保存");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("文件已存在，无需保存哈希表");
        }
    }

    private void allocateChunk() {
        for (int i = 0; i < chunkCount; ++i) {
            Random random = new Random();
            int randomPortIndex = random.nextInt(SERVER_PORTS.length);
            if (hash.containsKey(SERVER_PORTS[randomPortIndex])) {
                List<Integer> v = hash.get(SERVER_PORTS[randomPortIndex]);
                v.add(i);
                hash.put(SERVER_PORTS[randomPortIndex], v);
            } else {
                hash.put(SERVER_PORTS[randomPortIndex], new ArrayList<>(Arrays.asList(i)));
            }
        }
    }

    private void initSocket(Socket[] sockets, DataOutputStream[] dataOutputStreams) throws IOException {
        for (int i = 0; i < sockets.length; ++i) {
            // 跳过不需要的服务器
            if (!hash.containsKey(SERVER_PORTS[i])) {
                continue;
            }
            sockets[i] = new Socket(SERVER_HOST, SERVER_PORTS[i]);
            // 发送对应端口分配的块号
            dataOutputStreams[i] = new DataOutputStream(sockets[i].getOutputStream());
            DataOutputStream dataOutputStream = dataOutputStreams[i];
            // 发送请求类型
            dataOutputStream.write(request.getBytes(StandardCharsets.UTF_8));
            // 发送文件名
            String fileNameWithExt = fileName + "." + fileExt;
            byte[] fileNameByte = fileNameWithExt.getBytes();
            dataOutputStream.writeInt(fileNameByte.length);
            dataOutputStream.write(fileNameByte);

            List<Integer> v = hash.get(SERVER_PORTS[i]);

            // 发送块分配结果
            // 发送个数
            dataOutputStream.writeInt(v.size());
            // 发送每个块号
            for (int chunkNumber: v) {
                dataOutputStream.writeInt(chunkNumber);
            }

//            sockets[i].shutdownOutput();
            // 不能关闭输出流了 待会还要用
        }
    }

    public static void main(String[] args) {
        CUpload cUpload = new CUpload("test.txt");
        cUpload.getChunk();
        cUpload.getHashTable();
        cUpload.sendChunk();
        cUpload.saveHashTable();
    }
}

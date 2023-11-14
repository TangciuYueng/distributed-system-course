package org.cn.edu.tongji.server;

import org.cn.edu.tongji.util.SendFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CDownload {
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Socket socket;
    private List<String> requiredChunkFileNames;
    private String basePath;

    public CDownload(Socket socket, String basePath) throws IOException {
        this.socket = socket;
        this.basePath = basePath;
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        requiredChunkFileNames = new ArrayList<>();
    }

    private void releaseStream() throws IOException {
        dataOutputStream.close();
        dataOutputStream.close();
    }
    private void sendChunkFile() throws IOException {
        for (String fileName: requiredChunkFileNames) {
            Path filePath = Paths.get(basePath, fileName);
            try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
                SendFile sendFile = new SendFile(fileName, fileChannel, dataOutputStream);
                sendFile.send();

                System.out.println("发送文件成功" + fileName);
            }
        }
    }

    private void getChunkFileName() throws IOException {
        int fileCount = dataInputStream.readInt();
        for (int i = 0; i < fileCount; ++i) {
            int fileNameLength = dataInputStream.readInt();
            byte[] chunkName = new byte[fileNameLength];
            dataInputStream.readFully(chunkName);
            String fileName = new String(chunkName, StandardCharsets.UTF_8);
            requiredChunkFileNames.add(fileName);
        }
    }
    public void handleCDownload() throws IOException {
        getChunkFileName();
        System.out.println("需要的文件" + requiredChunkFileNames);
        sendChunkFile();
        releaseStream();
    }
}

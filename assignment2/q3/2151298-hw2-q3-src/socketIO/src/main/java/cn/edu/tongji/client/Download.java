package cn.edu.tongji.client;

import cn.edu.tongji.util.ReceiveFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;

public class Download {
    protected final String fileName;
    protected final String basePath;
//    protected int chunkCount;
    protected AtomicInteger chunkCount = new  AtomicInteger(0);
    protected static int[] SERVER_PORTS = {8887, 8888, 8889};
    private static final String request = "D";
    protected static final String SERVER_HOST = "localhost";

    public Download(String fileName) {
        this.fileName = fileName;
        this.basePath = "files_download";
    }
    protected void getChunk() {
        for (int port: SERVER_PORTS) {
            try (Socket socket = new Socket(SERVER_HOST, port);
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
                // sent request type
                dataOutputStream.write(request.getBytes(StandardCharsets.UTF_8));
                // sent requested file name
                byte[] fileNameByte = fileName.getBytes(StandardCharsets.UTF_8);
                dataOutputStream.writeInt(fileNameByte.length);
                dataOutputStream.write(fileNameByte);

                // get the number of file chunks, just for this port
                int chunkFileCount = dataInputStream.readInt();
                // get the number of total chunks
                chunkCount.addAndGet(chunkFileCount);
                // get each file chunk
                for (int i = 0; i < chunkFileCount; ++i) {
                    // get the length of each file name
                    int fileNameLength = dataInputStream.readInt();
                    // get the content of the file
                    ReceiveFile receiveFile = new ReceiveFile(fileNameLength, basePath, dataInputStream);
                    receiveFile.receive();
                }

            } catch (Exception e) {
                System.out.println(port + " connected failed");
            }
        }
    }
    protected void mergeFile() {
        Path targetFilePath = Paths.get(basePath, fileName);
        Path parentDir = targetFilePath.getParent();
        // check the folder exists or not
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                System.out.println("create dir error");
            }
        }
        // merge file chunks
        try (FileChannel fileChannel = FileChannel.open(targetFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (int i = 0; i < chunkCount.get(); ++i) {
                Path chunkFilePath = Paths.get(basePath, fileName + "$" + i);
                try (FileChannel chunkFileChannel = FileChannel.open(chunkFilePath, StandardOpenOption.READ)) {
                    fileChannel.position(fileChannel.size());
                    chunkFileChannel.transferTo(0, chunkFileChannel.size(), fileChannel);
                }
            }
            System.out.println(fileName + " download successfully, check the folder files_download");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("create target file failed");
        }
    }
    protected void deleteChunkFile() {
        for (int i = 0; i < chunkCount.get(); ++i) {
            int chunkIndex = i;
            // delete temp file chunks
            Path chunkFilePath = Paths.get(basePath, fileName + "$" + chunkIndex);
            if (Files.exists(chunkFilePath)) {
                try {
                    Files.delete(chunkFilePath);
                } catch (IOException e) {
                    System.out.println(chunkFilePath + "deleted failed");
                }
            }
        }
    }

    protected boolean fileExists() {
        return chunkCount.get() > 0;
    }

    public static void downloadFile(String fileName) {
        Download download = new Download(fileName);
        download.getChunk();
        if (download.fileExists()) {
            download.mergeFile();
            download.deleteChunkFile();
        } else {
            System.out.println(fileName + " get failed");
        }
    }

    public static void main(String[] args) {
        Download download = new Download("test.pdf");
        download.getChunk();
        if (download.fileExists()) {
            download.mergeFile();
            download.deleteChunkFile();
        } else {
            System.out.println("test.txt doesn't exists");
        }
    }
}

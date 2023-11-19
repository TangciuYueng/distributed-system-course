package cn.edu.tongji.server;

import cn.edu.tongji.util.ReceiveFile;
import cn.edu.tongji.util.SendFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class Server implements Runnable {
    private final int serverPort;
    private final String basePath;
    // request char mapping to the handle function
    private final Map<Character, RequestHandler> requestHandles = new HashMap<>();
    public Server(int serverPort, String basePath) {
        this.serverPort = serverPort;
        this.basePath = basePath;

        // init the map
        requestHandles.put('U', this::handleUpload);
        requestHandles.put('D', this::handleDownload);
        requestHandles.put('P', this::handleCUpload);
        requestHandles.put('G', this::handleCDownload);
        requestHandles.put('C', this::handleCheck);
    }

    @FunctionalInterface
    interface RequestHandler {
        void handle(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws Exception;
    }

    private void handleCheck(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        // get the file name
        String fileName = getFileName(dataInputStream);
        // get the file chunk set
        Set<Integer> chunkIndex = getChunkSet(basePath, fileName + ".ser");
        System.out.println("get the set " + chunkIndex);
        // send the number of the file chunks
        dataOutputStream.writeInt(chunkIndex.size());
        // send each file chunk index
        for (int index: chunkIndex) {
            dataOutputStream.writeInt(index);
        }
    }

    private void handleCDownload(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        // record required file names
        List<String> requiredFileNames = new ArrayList<>();
        // get the file name
        String fileName = getFileName(dataInputStream);
        // get the number of the required file names
        int fileNameCount = dataInputStream.readInt();
        for (int i = 0; i < fileNameCount; ++i) {
            requiredFileNames.add(getFileName(dataInputStream));
        }
        // send required files
        for (String chunkFileName: requiredFileNames) {
            Path chunkFilePath = Paths.get(basePath, chunkFileName);
            // send file
            try (FileChannel fileChannel = FileChannel.open(chunkFilePath, StandardOpenOption.READ)) {
                SendFile sendFile = new SendFile(chunkFileName, fileChannel, dataOutputStream);
                sendFile.send();
                // get successful flag
                dataInputStream.readInt();
                System.out.println(chunkFilePath + " is sent successfully");
            } catch (IOException e) {
                System.out.println(chunkFilePath + " open error");
            }
        }
    }

    private void handleCUpload(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws Exception {
        handleUpload(dataInputStream, dataOutputStream);
    }

    private void handleDownload(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws IOException {
        // get the file name
        String fileName = getFileName(dataInputStream);
        // get the file chunk set
        Set<Integer> chunkIndex = getChunkSet(basePath, fileName + ".ser");
        System.out.println("get the set " + chunkIndex);
        // send the number of the file chunks
        dataOutputStream.writeInt(chunkIndex.size());
        // send each file
        for (int i: chunkIndex) {
            Path chunkFilePath = Paths.get(basePath, fileName + "$" + i);
            // send file
            try (FileChannel fileChannel = FileChannel.open(chunkFilePath, StandardOpenOption.READ)) {
                SendFile sendFile = new SendFile(fileName + "$" + i, fileChannel, dataOutputStream);
                sendFile.send();
                System.out.println(chunkFilePath + " is sent successfully");
            } catch (IOException e) {
                System.out.println(chunkFilePath + " open error");
            }
        }
    }

    private Set<Integer> getChunkSet(String basePath, String fileName) {
        Path filePath = Paths.get(basePath, fileName);
        Set<Integer> chunkIndex;
        try {
            chunkIndex = Files.lines(filePath)
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            chunkIndex = new HashSet<>();
        }
        return chunkIndex;
    }

    private void handleUpload(DataInputStream dataInputStream, DataOutputStream dataOutputStream) throws Exception {
        // get the file name
        String fileName = getFileName(dataInputStream);

        // state a set for chunk index given
//        Set<Integer> chunkIndex = new HashSet<>();

        // get the number of the file names
        int fileNameCount = dataInputStream.readInt();
        // get each file chunk
        for (int i = 0; i < fileNameCount; ++i) {
            // get the length of each file name
            int fileNameLength = dataInputStream.readInt();
            // get the content of the file
            ReceiveFile receiveFile = new ReceiveFile(fileNameLength, basePath, dataInputStream);
            // add into the chunk index set
//            chunkIndex.add(receiveFile.receive());
            int chunkIndex = receiveFile.receive();
            // get one save one
            saveChunkIndex(basePath, fileName + ".ser", chunkIndex);
            // send successful flag
            dataOutputStream.writeInt(0);
        }

        // save the file chunk index set
//        saveSet(basePath, fileName + ".ser", chunkIndex);
    }

    private void saveChunkIndex(String basePath, String fileName, int chunkIndex) throws IOException {
        // create the folder if not exists
        Path filePath = Paths.get(basePath, fileName);
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        // check if the file exists
        if (!Files.exists(filePath)) {
            // if the file doesn't exist, create a new file and write the chunkIndex
            String chunkIndexLine = chunkIndex + System.lineSeparator();
            Files.write(filePath, chunkIndexLine.getBytes(), StandardOpenOption.CREATE);
        } else {
            // if the file exists, append the chunkIndex
            String chunkIndexLine = chunkIndex + System.lineSeparator();
            Files.write(filePath, chunkIndexLine.getBytes(), StandardOpenOption.APPEND);
        }
    }

    private void saveSet(String basePath, String fileName, Set<Integer> chunkIndex) throws IOException {
        // create the folder if not exists
        Path filePath = Paths.get(basePath, fileName);
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        // get the set xxx.ser first
        Set<Integer> chunkIndexOrigin = getChunkSet(basePath, fileName);
        // get the union
        chunkIndex.addAll(chunkIndexOrigin);
        System.out.println("after union " + chunkIndex);
        // save the set into the local file xxx.ser
        Files.write(filePath, chunkIndex.stream().map(Object::toString).toList());
        System.out.println("mapping table is saved successfully " + chunkIndex);
    }

    private String getFileName(DataInputStream dataInputStream) throws IOException {
        int nameLength = dataInputStream.readInt();
        byte[] nameBytes = new byte[nameLength];
        dataInputStream.readFully(nameBytes);
        String fileName = new String(nameBytes, StandardCharsets.UTF_8);
        System.out.println("receive file name "  + fileName);
        return fileName;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println(serverPort + " server is on");
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("client connected: " + socket.getInetAddress() + serverPort);
                    handleClient(socket);
                }
            }
        } catch (IOException e) {
            System.out.println("server failed");
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
            // get the request type
            Character request = (char) dataInputStream.readByte();
            // call the corresponding method
            if (requestHandles.containsKey(request)) {
                RequestHandler handler = requestHandles.get(request);
                handler.handle(dataInputStream, dataOutputStream);
            } else {
                System.out.println("request error");
            }
        } catch (Exception e) {
            System.out.println("open socket IO stream failed");
        }
    }
}

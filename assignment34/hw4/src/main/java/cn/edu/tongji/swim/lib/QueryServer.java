package cn.edu.tongji.swim.lib;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class QueryServer {
    private static int PORT = 9000;
    private static String FILE_NAME = "output.json";
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                System.out.println("waiting...");
                Socket socket = serverSocket.accept();
                System.out.println("connection from " + socket);
                try (DataInputStream inFromClient = new DataInputStream(socket.getInputStream());
                     DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream())) {
                    String keyword = inFromClient.readUTF();
                    outToClient.writeInt(getCount(keyword));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static int getCount(String keyword) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(FILE_NAME, "r")) {
            byte[] data = new byte[(int) randomAccessFile.length()];
            randomAccessFile.readFully(data);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Map<String, Integer>> mapData = mapper.readValue(data, new TypeReference<>() {});

            Map<String, Integer> innerMap = mapData.get(keyword);
            if (innerMap != null) {
                return innerMap.getOrDefault(keyword, 0);
            }
            return 0;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

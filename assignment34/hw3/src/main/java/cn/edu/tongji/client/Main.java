package cn.edu.tongji.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Enter the name");
                String name = scanner.nextLine();

                if (name.equals("q")) {
                    break;
                }

                try (Socket socket = new Socket("localhost", 8080);
                     DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                     DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {
                    byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
                    dataOutputStream.writeInt(nameBytes.length);
                    dataOutputStream.write(nameBytes);

                    int length = dataInputStream.readInt();
                    if (length > 0) {
                        byte[] dataBytes = new byte[length];
                        dataInputStream.readFully(dataBytes);
                        String data = new String(dataBytes, StandardCharsets.UTF_8);
                        System.out.println(data);
                    } else {
                        System.out.println("Name not found");
                    }
                } catch (UnknownHostException e) {
                    // 处理 UnknownHostException
                    e.printStackTrace();
                } catch (IOException e) {
                    // 处理 IOException
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            // 处理 Scanner 关闭异常
            e.printStackTrace();
        }
    }
}

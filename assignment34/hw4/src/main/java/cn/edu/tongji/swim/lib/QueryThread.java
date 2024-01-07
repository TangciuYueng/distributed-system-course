package cn.edu.tongji.swim.lib;

import lombok.AllArgsConstructor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

@AllArgsConstructor
public class QueryThread implements Callable {
    private String keyword;
    private String address;
    private int port;

    @Override
    public Integer call() throws Exception {
        try (Socket connectionSocket = new Socket(address, port);
             DataOutputStream outToServer = new DataOutputStream(connectionSocket.getOutputStream());
             DataInputStream inFromServer = new DataInputStream(connectionSocket.getInputStream())
        ) {
            connectionSocket.setSoTimeout(3000);

            outToServer.writeUTF(keyword);

            int result = inFromServer.readInt();

            return  result;
        }
    }
}

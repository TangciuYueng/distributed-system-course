package cn.edu.tongji.server;

import cn.edu.tongji.tools.PersistentBTree;
import static cn.edu.tongji.tools.Query.customHashFunction;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class DataSearchThread implements Runnable {
    private final Socket connectionSocket;
    private final String author;
    private final PersistentBTree<String, Long>[] trees;

    public DataSearchThread(final Socket connectionSocket, final String author, final PersistentBTree<String, Long>[] trees) {
        this.connectionSocket = connectionSocket;
        this.author = author;
        this.trees = trees;
    }

    @Override
    public void run() {
        try {
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
            final int bucketNum = customHashFunction(author) % 4;
            Long pointer = trees[bucketNum].search(author);

            if (pointer == null) {
                outToClient.writeBoolean(false);
                return;
            }

            outToClient.writeBoolean(true);
            outToClient.writeInt(bucketNum);
            outToClient.writeLong(pointer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

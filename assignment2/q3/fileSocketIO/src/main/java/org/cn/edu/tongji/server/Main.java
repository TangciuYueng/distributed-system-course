package org.cn.edu.tongji.server;

public class Main {
    public static void main(String[] args) {
        int[] serverPorts = {8887, 8888, 8889};
        String[] basePaths = {"server1_files", "server2_files", "server3_files"};
        Server[] servers = new Server[serverPorts.length];
        Thread[] threads = new Thread[serverPorts.length];

        for (int i = 0; i < serverPorts.length; i++) {
            Server server = new Server(serverPorts[i], basePaths[i]);
            servers[i] = server;
            threads[i] = new Thread(server);
            threads[i].start();
        }
        // 等待线程结束
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

package cn.edu.tongji.client;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.edu.tongji.server.Main.*;

public class Main {
    // 服务器列表
    static String[] hosts = new String[]{"124.221.224.31"};
    public static void main(String[] args) {
        while (true) {
            try (ExecutorService exec = Executors.newFixedThreadPool(7 * 4)) {
                System.out.print("请输入查询作者名：");
                final String author = new Scanner(System.in).nextLine();

                if (Objects.equals(author, "_break")) {
                    return;
                }
                for (String host: hosts) {
                    for (int i = 0; i < 4; i++) {
                        final int serverNum = i;
                        final int port = 9999 + i;
                        exec.execute(() -> new RequestThread(serverNum, author, host, port).run());
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

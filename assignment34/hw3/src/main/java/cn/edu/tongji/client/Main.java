package cn.edu.tongji.client;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    // 服务器列表
    static String[] hosts = new String[]{"124.221.224.31"};
    public static void main(String[] args) {
        while (true) {
            // 分配服务器数量 * 桶数量个数线程
            try (ExecutorService exec = Executors.newFixedThreadPool(7 * 4)) {
                System.out.print("请输入查询作者名：");
                final String author = new Scanner(System.in).nextLine();

                if (Objects.equals(author, "_break")) {
                    return;
                }
                // 向每台服务器发送 桶 数量的请求
                for (String host: hosts) {
                    for (int i = 0; i < 4; i++) {
                        final int serverNum = i;
                        final int port = 9999 + i;
                        exec.execute(() -> new RequestThread(serverNum, author, host, port).run());
                    }
                }
                // 需要增加 1. 超时处理 2. 对于冗余数据返回的处理，对于多台服务器中只要有一台查出了结果即可展示
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

package cn.edu.tongji.client;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.edu.tongji.server.Main.*;

public class Main {
    public static void main(String[] args) {
        while (true) {
            try (ExecutorService exec = Executors.newCachedThreadPool()) {
                System.out.print("请输入查询作者名：");
                final String author = new Scanner(System.in).nextLine();

                if (Objects.equals(author, "_break")) {
                    return;
                }

                for (int i = 0; i < SERVER_NUM; i++) {
                    final int serverNum = i;
                    exec.execute(() -> new RequestThread(serverNum, author).run());
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

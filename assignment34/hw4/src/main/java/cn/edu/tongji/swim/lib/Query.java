package cn.edu.tongji.swim.lib;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Query {
    private static int PORT = 9000;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        List<String> addresses = Arrays.asList("8.130.90.215", "124.221.224.31", "124.220.39.190", "122.51.113.192", "124.221.188.168", "8.130.89.193", "43.142.102.35", "8.130.173.131");

        while (true) {
            System.out.println("输入查询关键字");
            String keyword = scanner.nextLine();

            List<Future<Integer>> futures = new ArrayList<>();
            try (ExecutorService exec = Executors.newFixedThreadPool(addresses.size())) {

                for (String address: addresses) {
                    Future<Integer> future = exec.submit(() -> new QueryThread(keyword, address, PORT).call());
                    futures.add(future);
                }
            }

            System.out.println("查询结果");
            int total = 0;
            for (Future<Integer> future: futures) {
                try {
                    total += future.get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

            System.out.println(keyword + ": " + total + "个");
        }
    }
}

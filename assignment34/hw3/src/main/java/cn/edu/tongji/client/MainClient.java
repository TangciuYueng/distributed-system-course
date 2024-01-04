package cn.edu.tongji.client;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MainClient {
    // 服务器列表
    static String[] hosts = new String[]{"124.221.224.31","124.220.39.190"};
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
                        // 执行任务并获取一个 Future 对象
                        Future<?> future = exec.submit(() -> new RequestThreadClient(serverNum, author, host, port).run());
                        // 超时处理
                        try {
                            // 等待线程完成，超时时间设置为 10 秒
                            future.get(10, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            // 处理超时：取消线程
                            future.cancel(true);
                        }
                    }
                }
//                for (String host : hosts) {
//                    // 用于标记是否在其中一个线程中找到了数据
//                    boolean dataFound = false;
//
//                    for (int i = 0; i < 4; i++) {
//                        final int serverNum = i;
//                        final int port = 9999 + i;
//
//                        // 执行任务并获取一个 Future 对象
//                        Future<Boolean> future = exec.submit(() -> new RequestThreadClient(serverNum, author, host, port).run());
//
//                        try {
//                            // 等待线程完成，超时时间设置为 10 秒
//                            Boolean result = future.get(10, TimeUnit.SECONDS);
//
//                            // 检查线程是否已经完成并且找到了数据
//                            if (!future.isDone() || (future.isDone() && result)) {
//                                dataFound = true;
//                                // 处理其他线程不再等待的情况
//                                for (Future<?> otherFuture : exec.invokeAll(exec.shutdownNow())) {
//                                    otherFuture.cancel(true);
//                                }
//                                break;
//                            }
//                        } catch (Exception e) {
//                            // 处理超时或其他异常：取消线程
//                            future.cancel(true);
//                        }
//                    }
//
//                    if (dataFound) {
//                        // 在这里处理找到数据的情况
//                        System.out.println("在某个服务器上找到了数据！");
//                        break; // 跳出主循环，程序结束
//                    } else {
//                        // 在这里处理未找到数据的情况
//                        System.out.println("在所有服务器上均未找到数据。");
//                    }
//                }

                // 需要增加 对于冗余数据返回的处理，对于多台服务器中只要有一台查出了结果即可展示
                // 服务器上的块的排序

            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

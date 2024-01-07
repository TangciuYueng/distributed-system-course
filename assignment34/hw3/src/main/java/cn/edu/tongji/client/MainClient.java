package cn.edu.tongji.client;

import cn.edu.tongji.tools.SearchResult;
import cn.edu.tongji.tools.YearProcessor;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainClient {
    static int START_PORT = 9999;
    // 每个服务器对应的文件块号
    static Map<String, List<Integer>> hostToChunkNums = new HashMap<>();
    // 初始化每个服务器对应文件块号
    public static void initHostToChunkNums() {
//        hostToChunkNums.put("8.130.90.215", new ArrayList<>(Arrays.asList(6, 7, 1, 2)));
//        hostToChunkNums.put("124.221.224.31", new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5)));
//        hostToChunkNums.put("124.220.39.190", new ArrayList<>(Arrays.asList(6, 7, 1, 2, 3)));
//        hostToChunkNums.put("122.51.113.192", new ArrayList<>(Arrays.asList(4, 5, 6, 7, 1)));
//        hostToChunkNums.put("124.221.188.168", new ArrayList<>(Arrays.asList(2, 3, 4, 5)));
//        hostToChunkNums.put("8.130.89.193", new ArrayList<>(Arrays.asList(3, 4, 5, 6)));
//        hostToChunkNums.put("43.142.102.35", new ArrayList<>(Arrays.asList(7, 1, 2, 3)));
        hostToChunkNums.put("8.130.173.131", new ArrayList<>(Arrays.asList(4, 5, 6, 7)));
    }
    public static void main(String[] args) {
        initHostToChunkNums();

        while (true) {
            // 分配服务器数量 * 桶数量个数线程
            try (ExecutorService exec = Executors.newFixedThreadPool(7 * 4)) {
                int year1 = -1;
                int year2 = -1;

                Scanner scanner = new Scanner(System.in);
                System.out.print("\n请输入查询作者名: ");
                final String author = scanner.nextLine();
                if (Objects.equals(author, "_break")) {
                    return;
                }
                System.out.print("请输入起始年份，若忽略请输入-1: ");
                try {
                    year1 = scanner.nextInt();
                } catch (Exception e) {
                    System.out.println("请输入正确年份");
                    continue;
                }
                System.out.print("请输入结束年份，若忽略请输入-1: ");
                try {
                    year2 = scanner.nextInt();
                } catch (Exception e) {
                    System.out.println("请输入正确年份");
                    continue;
                }

                List<Future<SearchResult>> futures = new ArrayList<>();

                // 向每台服务器发送 桶 数量的请求
                for (Map.Entry<String, List<Integer>> entry: hostToChunkNums.entrySet()) {
                    String address = entry.getKey();
                    List<Integer> chunkNums = entry.getValue();
                    int portOffsest = 0;
                    for (int chunkNum: chunkNums) {
                        int port = START_PORT + portOffsest;
                        Future<SearchResult> future = exec.submit(() -> new RequestThread(chunkNum, author, address, port).call());
                        futures.add(future);
                        portOffsest++;
                    }
                }

                SearchResult shortestTimeResult = null;
                double shortestTime = Double.MAX_VALUE;
                // 等待每个线程的结果，并获取第一个为true的SearchResult
                for (Future<SearchResult> future : futures) {
                    try {
                        SearchResult result = future.get();
                        if (result != null && result.getFound()) {
                            double time = result.getTime();
                            if (time < shortestTime) {
                                shortestTime = time;
                                shortestTimeResult = result;
                            }
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
                // 找到人
                if (shortestTimeResult != null) {
                    int publication = YearProcessor.getTotalPublication(shortestTimeResult.getData(), year1, year2);
                    if (publication == 0) {
                        System.out.println(author + "没有在该时间范围内发表论文!");
                    } else {
                        System.out.println("搜索完毕!\n结果: 共" + publication + "篇\n用时: " + shortestTimeResult.getTime() + "s");
                    }
                } else {
                    // 没有人
                    System.out.println("查无此人，请检查输入!");
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}

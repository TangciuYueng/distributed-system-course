package cn.edu.tongji;

import cn.edu.tongji.swim.lib.Swim;
import cn.edu.tongji.swim.options.SwimOptions;
import cn.edu.tongji.swim.options.UdpOptions;

import java.util.*;
import java.util.concurrent.*;

public class Main {
//    private static final List<String> hosts = List.of(new String[] {
//            "121.40.220.228:8888",
//            "8.130.90.215:8888",
//            "124.221.188.168:8888",
//            "122.51.113.192:8888",
//            "124.220.39.190:8888",
//            "124.221.224.31:8888",
//            "8.130.89.193:8888",
//            "43.142.102.35:8888",
//            "8.130.173.131:8888"
//    });
    private static final List<String> hosts = List.of(new String[] {
        "100.80.195.159:8888",
        "100.80.195.159:8889",
        "100.80.195.159:8890"
    });

    public static Swim swim;
    public static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    public static void onBootstrap(String local, RuntimeException err) {
        if (err != null) {
            System.out.println(local + " bootstrap error: " + err.getMessage());
        }
    }

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        Timer printTimer = new Timer();
        Timer leaveTimer = new Timer();
        Timer rejoinTimer = new Timer();

//        exec.execute(() -> {
//            swim = new Swim(SwimOptions.builder()
//                    .udp(new UdpOptions())
//                    .local("100.80.195.159:8888")
//                    .codec("json")
//                    .build());
//            swim.bootstrap(hosts);
//
//            printTimer.scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//                    System.out.println("-------------------------------");
//                    System.out.println(swim.members(true, true));
//                    System.out.println();
//                }
//            }, 0, 1000);
//        });
        executorService.scheduleAtFixedRate(() -> {
            swim = new Swim(SwimOptions.builder()
                    .udp(new UdpOptions())
                    .local("100.80.195.159:8890")
                    .codec("json")
                    .build());
            swim.bootstrap(hosts);

            printTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("-------------------------------");
                    System.out.println(swim.members(true, true));
                    System.out.println();
                }
            }, 0, 1000);
        }, 0, 1000, TimeUnit.MILLISECONDS);

        /*setTimeout(leaveTimer, () -> {
            System.out.println("swim0 leaves");
            swims.get(0).leave();
        }, 1000 * 5);

        setTimeout(rejoinTimer, () -> {
            System.out.println("swim0 rejoin");
            swims.get(0).bootstrap(hosts);
        }, 1000 * 15);*/
    }
}

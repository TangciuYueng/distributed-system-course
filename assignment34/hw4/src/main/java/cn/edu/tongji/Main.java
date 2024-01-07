package cn.edu.tongji;

import cn.edu.tongji.swim.lib.Swim;
import cn.edu.tongji.swim.options.SwimOptions;
import cn.edu.tongji.swim.options.UdpOptions;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final List<String> hosts = List.of(new String[] {
            "121.40.220.228:8888",
            "8.130.90.215:8888",
            "124.221.188.168:8888",
            "122.51.113.192:8888",
            "124.220.39.190:8888",
            "124.221.224.31:8888",
            "8.130.89.193:8888",
            "43.142.102.35:8888",
            "8.130.173.131:8888"
    });

    public static Swim swim;
    public static OutputStreamWriter out;
    public static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

    public static void log(String info) {
        try {
            out.write(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onBootstrap(String local, RuntimeException err) {
        if (err != null) {
            System.out.println(local + " bootstrap error: " + err.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            out = new OutputStreamWriter(new FileOutputStream("log.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExecutorService exec = Executors.newCachedThreadPool();
        Timer printTimer = new Timer();
        Timer leaveTimer = new Timer();
        Timer rejoinTimer = new Timer();

        swim = new Swim(SwimOptions.builder()
                .udp(new UdpOptions())
                .local("8.130.173.131:8889")
                .codec("json")
                .build());
        swim.bootstrap(hosts);

        executorService.scheduleAtFixedRate(() -> {
            String log = "-------------------------------\n" +
                    swim.members(true, true).toString() + "\n\n";

            System.out.println(log);
            log(log);
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }
}

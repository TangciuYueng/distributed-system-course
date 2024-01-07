package cn.edu.tongji.swim.bench;

import cn.edu.tongji.swim.lib.Swim;
import cn.edu.tongji.swim.options.SwimOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import static cn.edu.tongji.swim.lib.JsTime.*;

public class Dumb {
    private static final List<String> hosts = List.of(new String[] {
            "121.40.220.228",
            "8.130.90.215",
            "124.221.188.168",
            "122.51.113.192",
            "124.220.39.190",
            "124.221.224.31",
            "8.130.89.193",
            "43.142.102.35",
            "8.130.173.131"
    });

    public static void onBootstrap(String local, RuntimeException err) {
        if (err != null) {
            System.out.println(local + "bootstrap error");
        }
    }

    public static void main(String[] args) {
        List<Swim> swims = new ArrayList<>();

        for (String host : hosts) {
            swims.add(new Swim(SwimOptions.builder()
                    .local(host)
                    .codec("json")
                    .build()
            ));
        }

        for (int i = 0; i < swims.size(); i++) {
            if (i == 0)
                swims.get(i).bootstrap(new ArrayList<>());
            else
                swims.get(i).bootstrap(hosts);
        }

        Timer printTimer = new Timer();
        Timer leaveTimer = new Timer();
        Timer rejoinTimer = new Timer();

        printTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("-------------------------------");

                for (Swim swim : swims) {
                    System.out.println(swim.localhost() + ' ' + swim.members(true, true));
                }

                System.out.println();
            }
        }, 0, 1000);

        setTimeout(leaveTimer, () -> {
            System.out.println("swim0 leaves");
            swims.get(0).leave();
        }, 1000 * 5);

        setTimeout(rejoinTimer, () -> {
            System.out.println("swim0 rejoin");
            swims.get(0).bootstrap(hosts);
        }, 1000 * 15);
    }
}

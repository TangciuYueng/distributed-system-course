package cn.edu.tongji.swim.lib;

import java.util.Timer;
import java.util.TimerTask;

public class JsTime {
    public static void setTimeout(Timer timer, Runnable task, int timeout) {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };

        timer.schedule(timerTask, timeout);
    }
}

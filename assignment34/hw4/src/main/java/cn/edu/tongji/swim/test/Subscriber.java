package cn.edu.tongji.swim.test;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class Subscriber {
    // 订阅事件 事件发送时候调用
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TestEventBus.MessageEvent event) {
        String message = event.getMessage();
        System.out.println("Received message: " + message);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent2(TestEventBus.MessageEvent event) {
        String message = event.getMessage();
        System.out.println("Received message2323: " + message);
    }
}

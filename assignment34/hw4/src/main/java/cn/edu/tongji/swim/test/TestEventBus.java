package cn.edu.tongji.swim.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TestEventBus {
    // 事件类 用于在不同组件之间传递消息
    @AllArgsConstructor
    @Data
    public class MessageEvent {
        private String message;
    }

    EventBus eventBus = new EventBus();

    void start() {
        eventBus.register(this);
    }

    void stop() {
        eventBus.unregister(this);
    }

    // 发送事件
    void sendMessage() {
        MessageEvent event = new MessageEvent("Hello, EventBus!");
        eventBus.post(event);
    }

    // 订阅事件 事件发送时候调用
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        String message = event.getMessage();
        System.out.println("Received message: " + message);
    }

    public static void main(String[] args) {
        TestEventBus test = new TestEventBus();
        test.start();
        test.sendMessage();
        test.stop();
    }
}

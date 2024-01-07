package cn.edu.tongji.swim.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.greenrobot.eventbus.EventBus;

import java.util.Scanner;

public class TestEventBus {
    Subscriber subscriber = new Subscriber();

    // 事件类 用于在不同组件之间传递消息
    @AllArgsConstructor
    @Data
    public class MessageEvent {
        private String message;
    }

    EventBus eventBus = new EventBus();

    void start() {
        eventBus.register(subscriber);
    }

    void stop() {
        eventBus.unregister(subscriber);
    }

    // 发送事件
    void sendMessage(String input) {
        MessageEvent event = new MessageEvent(input);
        eventBus.post(event);
    }

//    public static void main(String[] args) {
//        TestEventBus test = new TestEventBus();
//        test.start();
//        Scanner scanner = new Scanner(System.in);
//        String input;
//        while (true) {
//            input = scanner.nextLine();
//            test.sendMessage(input);
//            if (input.equalsIgnoreCase("q")) {
//                break;
//            }
//        }
//        test.stop();
//        scanner.close();
//    }
}

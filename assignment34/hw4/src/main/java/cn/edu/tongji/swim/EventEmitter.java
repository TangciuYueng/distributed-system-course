package cn.edu.tongji.swim;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

interface EventHandler {
    void handle(Object... args);
}

public class EventEmitter {
    private final Map<String, List<EventHandler>> eventHandlersMap = new HashMap<>();

    public void on(String eventName, EventHandler handler) {
        eventHandlersMap.computeIfAbsent(eventName, value -> new ArrayList<>()).add(handler);
    }

    public void emit(String eventName, Object... args) {
        List<EventHandler> handlers = eventHandlersMap.get(eventName);

        if (handlers != null) {  //若对应事件的处理方法存在，则逐个调用处理方法
            for (EventHandler handler : handlers) {
                handler.handle(args);  //这里支持一个事件多个处理方法，但请确保参数安排正确
            }
        }
    }


}

/*public class Main {
    public static void main(String[] args) {
        EventEmitter emitter = new EventEmitter();

        emitter.on("event1", new EventHandler() {
            @Override
            public void handle(Object... args) {
                System.out.println("Event 1 handled");
            }
        });

        emitter.on("event2", new EventHandler() {
            @Override
            public void handle(Object... args) {
                System.out.println("Event 2 handled with args: " + args[0]);
            }
        });

        emitter.emit("event1");
        emitter.emit("event2", "arg1");
    }
}*/


package cn.edu.tongji.swim.test;

import cn.edu.tongji.swim.netEvents.NetEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class TestNetEvent {
    EventBus eventBus = new EventBus();
    void start() {
        eventBus.register(this);
    }

    void stop() {
        eventBus.unregister(this);
    }
    @Subscribe
    public void onPingMessage(PingEvent event) {
        System.out.println(event.getRinfo());
    }

    public void sendMessage() {
        PingEvent event = new PingEvent(null, new NetEvent.Rinfo("1234", 22));
        eventBus.post(event);
    }

    public static void main(String[] args) {
        TestNetEvent test = new TestNetEvent();
        test.start();
        test.sendMessage();
        test.stop();
    }
}

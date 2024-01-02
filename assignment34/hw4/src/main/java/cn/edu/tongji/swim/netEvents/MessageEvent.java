package cn.edu.tongji.swim.netEvents;

public class MessageEvent extends NetEvent {
    public MessageEvent(byte[] buffer, Rinfo rinfo) {
        super(buffer, rinfo);
    }
}

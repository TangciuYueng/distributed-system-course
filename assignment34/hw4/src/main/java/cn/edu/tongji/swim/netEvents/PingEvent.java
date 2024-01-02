package cn.edu.tongji.swim.netEvents;

public class PingEvent extends NetEvent {
    public PingEvent(byte[] buffer, Rinfo rinfo) {
        super(buffer, rinfo);
    }
}

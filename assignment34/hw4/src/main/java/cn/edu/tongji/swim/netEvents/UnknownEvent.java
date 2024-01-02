package cn.edu.tongji.swim.netEvents;

public class UnknownEvent extends NetEvent {
    public UnknownEvent(byte[] buffer, Rinfo rinfo) {
        super(buffer, rinfo);
    }
}

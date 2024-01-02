package cn.edu.tongji.swim.netEvents;

public class AckEvent extends NetEvent {
    public AckEvent(byte[] buffer, Rinfo rinfo) {
        super(buffer, rinfo);
    }
}

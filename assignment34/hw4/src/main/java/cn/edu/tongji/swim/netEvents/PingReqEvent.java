package cn.edu.tongji.swim.netEvents;

public class PingReqEvent extends NetEvent {
    public PingReqEvent(byte[] buffer, Rinfo rinfo) {
        super(buffer, rinfo);
    }
}

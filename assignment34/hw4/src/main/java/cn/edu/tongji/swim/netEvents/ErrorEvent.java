package cn.edu.tongji.swim.netEvents;

public class ErrorEvent extends NetEvent {
    public ErrorEvent(byte[] buffer, Rinfo rinfo) {
        super(buffer, rinfo);
    }
}

package cn.edu.tongji.swim.netEvents;

public class SyncEvent extends NetEvent {
    public SyncEvent(byte[] buffer, Rinfo rinfo) {
        super(buffer, rinfo);
    }
}

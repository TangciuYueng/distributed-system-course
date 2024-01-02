package cn.edu.tongji.swim.netEvents;

public class UpdateEvent extends NetEvent {
    public UpdateEvent(byte[] buffer, Rinfo rinfo) {
        super(buffer, rinfo);
    }
}

package cn.edu.tongji.swim.netEvents;

public class ListeningEvent extends NetEvent {
    public ListeningEvent(byte[] buffer, Rinfo rinfo) {
        super(buffer, rinfo);
    }
}

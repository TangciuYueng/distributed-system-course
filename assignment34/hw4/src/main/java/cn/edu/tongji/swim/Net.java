package cn.edu.tongji.swim;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.DatagramPacket;
import java.util.Arrays;

@Data
public class Net {
    @AllArgsConstructor
    @Data
    public static class Udp {
        int port;
        String type;
        int maxDiagramSize;
    }

    public enum EventType {
        Error,
        Listening,
        Ping,
        PingReq,
        Sync,
        Ack,
        Update,
        Unknown
    }

    private Swim swim;
    private Udp udp;
    private DatagramPacket udpSocket;

    public static final int MessageTypeSize = 1;
    public static final int LengthSize = 2;


}

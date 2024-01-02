package cn.edu.tongji.swim.netEvents;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;

@Data
@AllArgsConstructor
public class ListeningEvent {
    private InetAddress address;
    private int port;
    private int maxDgramSize;
}

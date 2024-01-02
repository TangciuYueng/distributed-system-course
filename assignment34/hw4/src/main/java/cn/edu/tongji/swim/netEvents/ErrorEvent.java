package cn.edu.tongji.swim.netEvents;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.InetAddress;

@Data
@AllArgsConstructor
public class ErrorEvent {
    private InetAddress address;
    private int port;
    private String message;
}

package cn.edu.tongji.swim.netEvents;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PingReqEvent {
    private int seq;
    private String dest;
    private String host;
}

package cn.edu.tongji.swim.netEvents;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PingEvent {
    private int seq;
    private String host;
}

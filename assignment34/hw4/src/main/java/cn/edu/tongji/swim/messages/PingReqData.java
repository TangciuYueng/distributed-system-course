package cn.edu.tongji.swim.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PingReqData {
    private int seq;
    private String dest;
}

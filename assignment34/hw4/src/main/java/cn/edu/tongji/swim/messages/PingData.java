package cn.edu.tongji.swim.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PingData {
    private int seq;
}

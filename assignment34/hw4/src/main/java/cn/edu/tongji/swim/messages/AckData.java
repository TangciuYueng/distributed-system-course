package cn.edu.tongji.swim.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AckData {
    private int seq;
    private String host;
}

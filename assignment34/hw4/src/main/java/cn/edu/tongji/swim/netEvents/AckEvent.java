package cn.edu.tongji.swim.netEvents;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AckEvent {
    private Integer seq;
    private String host;
}

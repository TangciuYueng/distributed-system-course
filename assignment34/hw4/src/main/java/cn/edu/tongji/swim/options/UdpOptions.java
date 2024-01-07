package cn.edu.tongji.swim.options;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UdpOptions {
    private Integer port;
    private Integer maxDgramSize;
}

package cn.edu.tongji.swim.options;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UdpOptions {
    public Integer port;
    public Integer maxDgramSize;
}

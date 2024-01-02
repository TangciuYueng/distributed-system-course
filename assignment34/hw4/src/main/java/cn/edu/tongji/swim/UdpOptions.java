package cn.edu.tongji.swim;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UdpOptions {
    public Integer port;
    public Integer maxDgramSize;
}

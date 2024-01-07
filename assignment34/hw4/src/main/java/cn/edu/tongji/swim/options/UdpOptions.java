package cn.edu.tongji.swim.options;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UdpOptions {
    private Integer port;
    private Integer maxDgramSize;
}

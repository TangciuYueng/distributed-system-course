package cn.edu.tongji.swim.options;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FDOptions {
    private Integer interval;
    private Integer pingTimeout;
    private Integer pingReqTimeout;
    private Integer pingReqGroupSize;
}

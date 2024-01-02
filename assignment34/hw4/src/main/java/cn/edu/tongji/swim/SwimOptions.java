package cn.edu.tongji.swim;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwimOptions {
    private Swim swim;
    private String codec;
    private int disseminationFactor;
    private int disseminationLimit;
    private int interval;
    private int pingTimeout;
    private int pingReqTimeout;
    private int pingReqGroupSize;
    private String local;
    private int suspectTimeout;
    private boolean preferCurrentMeta;
    private UdpOptions udp;
    private int joinTimeout;
    private int joinCheckInterval;
}

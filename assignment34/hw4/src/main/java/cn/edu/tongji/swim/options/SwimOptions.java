package cn.edu.tongji.swim.options;

import cn.edu.tongji.swim.lib.Swim;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class SwimOptions {
    private String codec;
    private Integer disseminationFactor;
    private Integer interval;
    private Integer pingTimeout;
    private Integer pingReqTimeout;
    private Integer pingReqGroupSize;
    private String local;
    private Integer suspectTimeout;
    private Boolean preferCurrentMeta;
    private UdpOptions udp;
    private Integer joinTimeout;
    private Integer joinCheckInterval;
}

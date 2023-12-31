package cn.edu.tongji.basicGossip.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Duration;

@AllArgsConstructor
@Data
public class GossipConfig {
    private Duration failureTimeout;
    private Duration cleanupTimeout;
    private Duration updateFrequency;
    private Duration failureDetectionFrequency;
    private int NumOfNodeUpdated;
}

package cn.edu.tongji.gossip;

import cn.edu.tongji.config.GossipConfig;
import lombok.Data;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Node {
    // 网络地址和端口号
    private InetSocketAddress address;
    // 记录节点最后一次更新的时间戳
    private LocalDateTime lastUpdateTime;
    // 节点的配置信息
    private GossipConfig config;
    // 心跳序号
    private long heartbeatSeqNum;
    // 标记是否为 failure
    private boolean failed;
    // 唯一标识节点
    private UUID id = UUID.randomUUID();

    public Node(InetSocketAddress address, GossipConfig config, long heartbeatSeqNum) {
        validateParameters(address, config);
        this.address = address;
        this.config = config;
        this.heartbeatSeqNum = heartbeatSeqNum;
        updateLastUpdateTime();
        failed = false;
    }

    public InetAddress getInetAddress() {
        return address.getAddress();
    }

    public int getPort() {
        return address.getPort();
    }

    public void updateSeqNum(long newSeqNum) {
        if (newSeqNum > heartbeatSeqNum) {
            heartbeatSeqNum = newSeqNum;
            updateLastUpdateTime();
        }
    }

    private void validateParameters(InetSocketAddress address, GossipConfig config) {
        if (address == null || config == null) {
            throw new IllegalArgumentException("Address and config must not be null");
        }
    }

    // 更新为现在时间
    public void updateLastUpdateTime() {
        lastUpdateTime = LocalDateTime.now();
    }

    // 发送心跳给别人时增加序号
    public void incrementSeqNum() {
        heartbeatSeqNum++;
        updateLastUpdateTime();
    }

    // 检查是否 failure 超过 failureTimeout 就记录
    public void checkIfFailed() {
        LocalDateTime failureTime = lastUpdateTime.plus(config.getFailureTimeout());
        LocalDateTime now = LocalDateTime.now();
        failed = now.isAfter(failureTime);
    }

    // 检查节点是否应该移除 memberList
    public boolean shouldCleanup() {
        if (failed) {
            // 超过 cleanTimeout 需要清除
            Duration cleanupTimeout = config.getFailureTimeout().plus(config.getCleanupTimeout());
            LocalDateTime cleanupTime = lastUpdateTime.plus(cleanupTimeout);
            LocalDateTime now = LocalDateTime.now();
            return now.isAfter(cleanupTime);
        } else {
            return false;
        }
    }

    public String toString() {
        String nodeInfo = address != null ? address.getHostName() + ":" + address.getPort() + "-" + heartbeatSeqNum : "no address";
        return "[" + nodeInfo + "]";
    }

}

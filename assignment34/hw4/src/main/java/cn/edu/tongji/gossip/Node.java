package cn.edu.tongji.gossip;

import cn.edu.tongji.config.GossipConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Node {
    private InetSocketAddress address;
    private LocalDateTime lastUpdateTime;
    private GossipConfig config;
    private long heartbeatSeqNum;
    private boolean failed;
    private UUID id = UUID.randomUUID();

    public Node(InetSocketAddress address, GossipConfig config, long heartbeatSeqNum) {
        if (address == null || config == null) {
            throw new IllegalArgumentException("Address and config must not be null");
        }
        this.address = address;
        this.config = config;
        this.heartbeatSeqNum = heartbeatSeqNum;
        setLastUpdateTime();
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
            setLastUpdateTime();
        }
    }

    public void setLastUpdateTime() {
        LocalDateTime now = LocalDateTime.now();
        lastUpdateTime = now;
    }

    public void incrementSeqNum() {
        heartbeatSeqNum++;
        setLastUpdateTime();
    }

    public void checkIfFailed() {
        LocalDateTime failureTime = lastUpdateTime.plus(config.getFailureTimeout());
        LocalDateTime now = LocalDateTime.now();
        failed = now.isAfter(failureTime);
    }

    public boolean shouldCleanup() {
        if (failed) {
            Duration cleanupTimeout = config.getFailureTimeout().plus(config.getCleanupTimeout());
            LocalDateTime cleanupTime = lastUpdateTime.plus(cleanupTimeout);
            LocalDateTime now = LocalDateTime.now();
            return now.isAfter(cleanupTime);
        } else {
            return false;
        }
    }

    public String toString() {
        if (address != null) {
            return "[" + address.getHostName() + ":" + address.getPort() + "-" + heartbeatSeqNum + "]";
        } else {
            return "no address";
        }
    }

}

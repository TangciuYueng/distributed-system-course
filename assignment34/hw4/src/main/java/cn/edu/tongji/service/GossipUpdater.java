package cn.edu.tongji.service;

import java.net.InetSocketAddress;

public interface GossipUpdater {
    void update(InetSocketAddress address);
}

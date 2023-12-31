package cn.edu.tongji.basicGossip.service;

import java.net.InetSocketAddress;

public interface GossipUpdater {
    void update(InetSocketAddress address);
}

package cn.edu.tongji.basicGossip;

import cn.edu.tongji.basicGossip.config.GossipConfig;
import cn.edu.tongji.basicGossip.service.GossipService;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
//    public static void main(String[] args) throws SocketException {
//        GossipConfig config = new GossipConfig(
//                Duration.ofSeconds(3),
//                Duration.ofSeconds(3),
//                Duration.ofMillis(500),
//                Duration.ofMillis(500),
//                3
//        );
//
//        ExecutorService executorService = Executors.newCachedThreadPool();
//
//        GossipService initialNode = new GossipService(
//                new InetSocketAddress("127.0.0.1", 9090),
//                config
//        );
//
//        setGossipServiceHandlers(initialNode);
//
//        executorService.submit(initialNode::start);
//
//        for (int i = 1; i <= 10; i++) {
//            GossipService gossipService = new GossipService(
//                    new InetSocketAddress("127.0.0.1", 9090 + i),
//                    config
//            );
//
//            setGossipServiceHandlers(gossipService);
//
//            executorService.submit(gossipService::start);
//        }
//    }

    private static void setGossipServiceHandlers(GossipService gossipService) {
        gossipService.setOnNewMember((inetSocketAddress) ->
                LOGGER.log(Level.INFO, "Connected to {0}:{1}",
                        new Object[]{inetSocketAddress.getHostName(), inetSocketAddress.getPort()}));

        gossipService.setOnFailedMember((inetSocketAddress) ->
                LOGGER.log(Level.INFO, "Node {0}:{1} failed",
                        new Object[]{inetSocketAddress.getHostName(), inetSocketAddress.getPort()}));

        gossipService.setOnRemoveMember((inetSocketAddress) ->
                LOGGER.log(Level.INFO, "Node {0}:{1} removed",
                        new Object[]{inetSocketAddress.getHostName(), inetSocketAddress.getPort()}));

        gossipService.setOnReceivedMember((inetSocketAddress) ->
                LOGGER.log(Level.INFO, "Node {0}:{1} revived",
                        new Object[]{inetSocketAddress.getHostName(), inetSocketAddress.getPort()}));
    }
}

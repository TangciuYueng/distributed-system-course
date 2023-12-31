package cn.edu.tongji.basicGossip.service;

import cn.edu.tongji.basicGossip.config.GossipConfig;
import cn.edu.tongji.basicGossip.gossip.Node;
import lombok.Data;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

@Data
public class GossipService {
    private static final Logger LOGGER = Logger.getLogger(GossipService.class.getName());

    private InetSocketAddress address;
    private Node self;
    private ConcurrentHashMap<UUID, Node> nodes = new ConcurrentHashMap<>();
    private GossipConfig config;
    private SocketService service;
    private boolean stopped;
    
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(3);
    private final Random random = new Random();

    private GossipUpdater onNewMember;
    private GossipUpdater onFailedMember;
    private GossipUpdater onRemoveMember;
    private GossipUpdater onReceivedMember;

    private int getRandomIndex(int size) {
        return random.nextInt(size);
    }

    public GossipService(InetSocketAddress address, GossipConfig config) throws SocketException {
        this.address = address;
        this.config = config;

        self = new Node(address, config, 0);
        nodes.putIfAbsent(self.getId(), self);

        service = new SocketService(address.getPort());
    }

    public void start() {
        startSenderThread();
        startReceiverThread();
        startFailureDetectionThread();
        scheduleNodePrinting();
    }

    public void stop() {
        stopped = true;
    }

    private void scheduleNodePrinting() {
        executorService.scheduleAtFixedRate(() -> {
            printNodes();
        }, 0, 3, TimeUnit.SECONDS); // 每3秒打印一次    }
    }

    private void printNodes() {
        LOGGER.log(Level.INFO, "Printing nodes...");

        getMembers(false).forEach(node ->
                LOGGER.log(Level.INFO, "Health status: {0}:{1} - alive",
                        new Object[]{node.getHostName(), node.getPort()}));

        getMembers(true).forEach(node ->
                LOGGER.log(Level.INFO, "Health status: {0}:{1} - failed",
                        new Object[]{node.getHostName(), node.getPort()}));
    }

    private void startFailureDetectionThread() {
        executorService.scheduleAtFixedRate(() -> {
            detectFailedNodes();
        }, 0, config.getFailureDetectionFrequency().toMillis(), TimeUnit.MILLISECONDS);
    }

    private void startReceiverThread() {
        executorService.submit(() -> {
            while (!stopped) {
                receiveMessage();
            }
        });
    }

    private void receiveMessage() {
        Node newNode = service.receiveGossip();
        nodes.compute(newNode.getId(), (key, existingMember) -> {
            if (existingMember == null) {
                // 新加入的节点
                newNode.setConfig(config);
                newNode.updateLastUpdateTime();
                if (onNewMember != null) {
                    onNewMember.update(newNode.getAddress());
                }
                return newNode;
            } else {
                // 节点已经存在于映射表 更新现有节点的心跳序列
                LOGGER.log(Level.INFO, "Updating sequence number for node {0}", existingMember.getId());
                existingMember.setHeartbeatSeqNum(newNode.getHeartbeatSeqNum());
                return existingMember;
            }
        });
    }

    private void startSenderThread() {
        executorService.scheduleAtFixedRate(() -> {
            sendGossipToRandomNode();
        }, 0, config.getUpdateFrequency().toMillis(), TimeUnit.MILLISECONDS);
    }

    // 发送自己的信息给随机选中的节点
    private void sendGossipToRandomNode() {
        // 发送的同时增加自己的 seqNum 并且 更新时间戳
        self.incrementSeqNum();
        List<UUID> nodesToUpdate = new ArrayList<>(nodes.keySet());
        // 候选列表中移除自己
        nodesToUpdate.remove(self.getId());

        // 随机打乱
        Collections.shuffle(nodesToUpdate);

        // 发送给 config 中设置的数量和实际数量中的最小值
        int peersCount = Math.min(config.getNumOfNodeUpdated(), nodesToUpdate.size());
        for (int i = 0; i < peersCount; i++) {
            UUID targetKey = nodesToUpdate.get(i);
            Node node = nodes.get(targetKey);
            executorService.submit(() -> service.sendGossip(node, self));
        }
    }

    // 检查 failure 节点
    private void detectFailedNodes() {
        // 确保多个线程对 memberList 访问安全
        synchronized (nodes) {
            Iterator<Map.Entry<UUID, Node>> iterator = nodes.entrySet().iterator();
            // 遍历 memberList
            while (iterator.hasNext()) {
                Map.Entry<UUID, Node> entry = iterator.next();
                Node node = entry.getValue();
                // 记录节点先前的失败状态
                boolean hadFailed = node.isFailed();
                // 然后检查
                node.checkIfFailed();
                // 发送变化
                if (hadFailed != node.isFailed()) {
                    if (node.isFailed()) {
                        if (onFailedMember != null) {
                            onFailedMember.update(node.getAddress());
                        } else {
                            if (onReceivedMember != null) {
                                onReceivedMember.update(node.getAddress());
                            }
                        }
                    }
                }
                if (node.shouldCleanup()) {
                    iterator.remove();
                    if (onReceivedMember != null) {
                        onRemoveMember.update(node.getAddress());
                    }
                }
            }
        }
    }

    public ArrayList<InetSocketAddress> getMembers(boolean failed) {
        int initialSize = nodes.size();
        ArrayList<InetSocketAddress> aliveMembers =
                new ArrayList<>(initialSize);
        for (UUID key : nodes.keySet()) {
            Node node = nodes.get(key);
            if (node.isFailed() == failed) {
                String ipAddress = String.valueOf(node.getAddress());
                int port = node.getPort();
                aliveMembers.add(new InetSocketAddress(ipAddress, port));
            }
        }

        return aliveMembers;
    }

    public ArrayList<InetSocketAddress> getAllMembers() {
        // used to prevent resizing of ArrayList.
        int initialSize = nodes.size();
        ArrayList<InetSocketAddress> allMembers =
                new ArrayList<>(initialSize);

        for (UUID key : nodes.keySet()) {
            Node node = nodes.get(key);
            String ipAddress = String.valueOf(node.getAddress());
            int port = node.getPort();
            allMembers.add(new InetSocketAddress(ipAddress, port));
        }
        return allMembers;
    }
}

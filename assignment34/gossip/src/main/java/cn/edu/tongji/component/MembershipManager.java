package cn.edu.tongji.component;

import cn.edu.tongji.config.Config;
import lombok.Data;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class MembershipManager {
    private static String address;
    private MemberList memberList;

    public boolean isJoin;
    private HeartbeatSender sender;
    private Thread heartbeatSender;
    private Thread heartbeatReceiver;
    private Thread gossip;
    private Thread waitingForIntroducer;

    Logger logger;

    MembershipManager(String address) {
        MembershipManager.address = address;
        logger = new Logger(address);
        memberList = new MemberList();
        joinIn();
    }

    public void showMemberList() {
        memberList.show();
    }

    public void joinIn() {
        try {
            requestIntroducer(address);
            isJoin = true;
            startTreads();
        } catch (Exception e) {
            System.out.println("[error]: cannot connect to introducer");
        }
    }

    private void requestIntroducer(String address) throws UnknownHostException {
        InetAddress introducerAddress = InetAddress.getByName(Config.introducerAddress);
        byte[] data = address.getBytes();

        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket requestPacket = new DatagramPacket(data, data.length, introducerAddress, Config.introducerPort);

            String reply = sendRequestWithRetry(socket, requestPacket);
            handleJoinResponse(address, reply);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("[joinError]: " + address + " failed to join");
        }
    }

    private String sendRequestWithRetry(DatagramSocket socket, DatagramPacket requestPacket) {
        int maxRetries = 3;

        while (maxRetries > 0) {
            try {
                send(socket, requestPacket, Config.lossRate);

                byte[] responseBuffer = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                socket.setSoTimeout(10000);
                socket.receive(responsePacket);

                return new String(responseBuffer, 0, responsePacket.getLength());
            } catch (IOException e) {
                maxRetries--;
            }
        }

        return "";
    }

    private void handleJoinResponse(String address, String reply) {
        if (!reply.isEmpty()) {
            System.out.println("[join]: joined successfully！");
            logger.writeInfo("join " + address);
            String[] newMemberList = reply.split("\n");

            for (String newMember : newMemberList) {
                String newTimestamp = newMember.split(" ")[0];
                String newAddress = newMember.split(" ")[1];
                memberList.join(newTimestamp, newAddress);
            }
        } else {
            System.out.println("[joinError]:" + address + "failed to jion");
        }
    }

    private void startTreads() {
        if (isJoin) {
            ExecutorService executorService = Executors.newFixedThreadPool(4);

            executorService.submit(() -> {
                sender = new HeartbeatSender();
                heartbeatSender = new Thread(sender);
                heartbeatSender.start();
            });

            executorService.submit(() -> {
                heartbeatReceiver = new Thread(new HeartbeatReceiver());
                heartbeatReceiver.start();
            });

            executorService.submit(() -> {
                gossip = new Thread(new GossipingHandler());
                gossip.start();
            });

            executorService.submit(() -> {
                waitingForIntroducer = new Thread(new ListenIntroducer());
                waitingForIntroducer.start();
            });

            executorService.shutdown();
        }
    }

    public boolean lossPacket(double lossRate) {
        return new Random().nextDouble() <= lossRate;
    }

    public void send(DatagramSocket datagramSocket, DatagramPacket datagramPacket, double lossRate) throws IOException {
        boolean loss = lossPacket(lossRate);
        if (loss) {
            System.out.println("[info]: loss packet");
        } else {
            datagramSocket.send(datagramPacket);
        }
    }


    void sendNewMemberMessage() {
        try (DatagramSocket socket = new DatagramSocket(Config.broadcastPort)) {
            while (!waitingForIntroducer.isInterrupted()) {
                receiveAndProcessMessage(socket);
            }
        } catch (IOException e) {
            // 处理异常，可以根据实际情况进行调整
            e.printStackTrace();
        }
    }

    private void receiveAndProcessMessage(DatagramSocket socket) throws IOException {
        // 接收数据包
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        socket.receive(packet);

        // 接收数据
        String info = new String(packet.getData(), 0, packet.getLength());
        System.out.println("[new member]:新节点加入：" + info);

        // 处理新节点加入的信息
        processNewMemberInfo(info);

        // 响应
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        byte[] responseData = (address + "已收到新节点消息").getBytes();
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, address, port);
        socket.send(responsePacket);
    }

    private void processNewMemberInfo(String info) {
        String[] infoParts = info.split(" ");
        if (infoParts.length >= 2) {
            String timestamp = infoParts[0];
            String address = infoParts[1];
            memberList.join(timestamp, address);
        } else {
            System.out.println("[error]: 接收到的数据格式不正确");
        }
    }

    public void leave() throws IOException {
        String info = String.format("gossip %s leave %s", address, address);
        logger.writeInfo(info);

        // 逆向传播消息
        InetAddress lastServerAddress = InetAddress.getByName(memberList.lastServer(address));
        InetAddress introducerAddress = InetAddress.getByName(Config.introducerAddress);
        byte[] data = info.getBytes();

        DatagramPacket packet = new DatagramPacket(data, data.length, lastServerAddress, Config.gossipPort);
        DatagramPacket introducerPacket = new DatagramPacket(data, data.length, introducerAddress, Config.introducerListPort);

        try (DatagramSocket socket = new DatagramSocket()) {
            // 发送消息到上一个服务器和 introducer
            socket.send(packet);
            socket.send(introducerPacket);
        }

        // 从组成员列表中移除当前节点
        memberList.remove(address);

        // 中断各线程
        interruptThreads();

        isJoin = false;
        System.out.println("[leave]:" + address + "已主动离开");
    }

    private void interruptThreads() {
        heartbeatSender.interrupt();
        heartbeatReceiver.interrupt();
        gossip.interrupt();
    }


    public void handleNodeFailure(String failedNodeAddress) throws IOException {
        // 断言：检查要处理故障的节点是否是当前节点的下一个节点
        assert failedNodeAddress.equals(memberList.nextServer(address)) : "要处理故障的节点不是当前节点的下一个节点";

        // 打印日志
        System.out.println("[failure]:" + failedNodeAddress + "failed");

        // 更新本地逻辑数组
        memberList.remove(failedNodeAddress);

        // 更新自己要检查的节点
        sender.targetAddress = memberList.nextServer(address);

        // 重置错误计数
        sender.errorCount = 0;

        // 检查是否只剩下一个节点
        if (address.equals(sender.targetAddress)) {
            System.out.println("[warning]: only one member");
        }

        // 构建 gossip 协议消息
        String gossipMessage = String.format("gossip %s failure %s", address, failedNodeAddress);
        logger.writeInfo(gossipMessage);

        // 发送 gossip 协议消息
        sendGossipProtocol(gossipMessage, InetAddress.getByName(memberList.lastServer(address)), Config.gossipPort);
        sendGossipProtocol(gossipMessage, InetAddress.getByName(Config.introducerAddress), Config.introducerListPort);
    }

    private void sendGossipProtocol(String message, InetAddress address, int port) throws IOException {
        byte[] data = message.getBytes();
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            send(socket, packet, Config.lossRate);
        }
    }


    class GossipingHandler implements Runnable {
        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(Config.gossipPort)) {
                while (!gossip.isInterrupted()) {
                    byte[] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);

                    socket.receive(packet);
                    String gossipMessage = new String(data, 0, packet.getLength());

                    // Log received gossip message
                    logger.writeInfo(gossipMessage);

                    String[] messageParts = gossipMessage.trim().split("\\s+");

                    if (messageParts.length != 4 || !messageParts[0].equals("gossip")) {
                        continue;
                    }

                    String nextAddress = "";

                    if (messageParts[2].equals("failure")) {
                        System.out.println(messageParts[3]);
                        memberList.remove(messageParts[3]);
                        nextAddress = memberList.nextServer(address);
                    }

                    if (messageParts[2].equals("leave")) {
                        System.out.println(messageParts[3] + " leave");
                        nextAddress = handleLeaveGossip(messageParts[3]);
                    }

                    assert !nextAddress.isEmpty();

                    // Forward gossip
                    if (!nextAddress.equals(messageParts[1])) {
                        InetAddress address = InetAddress.getByName(nextAddress);
                        DatagramPacket forwardingPacket = new DatagramPacket(data, data.length, address, Config.gossipPort);
                        send(socket, forwardingPacket, Config.lossRate);
                    }
                }
            } catch (IOException e) {
                System.out.println("[error]:gossip接收错误！");
                e.printStackTrace();
            }
        }

        private String handleLeaveGossip(String leavingNodeAddress) {
            String nextIp = memberList.lastServer(address);

            if (sender.targetAddress.equals(leavingNodeAddress)) {
                memberList.remove(leavingNodeAddress);
                sender.targetAddress = memberList.nextServer(address);
                sender.errorCount = 0;
            } else {
                memberList.remove(leavingNodeAddress);
            }

            return nextIp;
        }
    }

    class ListenIntroducer implements Runnable {
        @Override
        public void run() {
            try {
                sendNewMemberMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class HeartbeatSender implements Runnable {
        public String targetAddress = memberList.nextServer(address);
        public final int interval = 5000;
        public int errorCount = 0;

        @Override
        public void run() {
            while (!heartbeatSender.isInterrupted()) {
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    return;
                }

                targetAddress = memberList.nextServer(address);

                if (errorCount >= Config.errorLimit) {
                    errorCount = 0;
                    try {
                        nodeFailure(targetAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                InetAddress address;
                DatagramPacket packet;
                try {
                    address = InetAddress.getByName(targetAddress);
                    byte[] data = (targetAddress + "here?").getBytes();
                    packet = new DatagramPacket(data, data.length, address, Config.heartbeatPort);

                } catch (UnknownHostException e) {
                    System.out.println("[error]:UnknownHostException");
                    System.out.println(e);
                    errorCount++;
                    continue;
                }

                try (DatagramSocket socket = new DatagramSocket()) {
                    send(socket, packet, Config.lossRate);

                    byte[] responseData = new byte[1024];
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);

                    socket.setSoTimeout(5000);
                    socket.receive(responsePacket);

                    String response = new String(responseData, 0, responsePacket.getLength());

                    socket.close();

                    if (response.equals("here")) {
                        errorCount = 0;
                    }

                } catch (IOException e) {
                    System.out.println("[error]:IOException");
                    System.out.println(e);
                    errorCount++;
                }
            }
        }

        private void nodeFailure(String failedNodeAddress) throws IOException {
            // Reset error count
            errorCount = 0;

            // Handle node failure, e.g., initiate leave process
            handleNodeFailure(failedNodeAddress);
        }
    }


    class HeartbeatReceiver implements Runnable {
        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket(Config.heartbeatPort)) {
                while (!heartbeatReceiver.isInterrupted()) {
                    // 创建数据报，用于接收客户端发送的数据
                    byte[] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);

                    // 接收客户端发送的数据
                    socket.receive(packet);

                    // 读取数据
                    String receivedInfo = new String(data, 0, packet.getLength());
//                    System.out.println("有人发来消息：" + receivedInfo);

                    // 向客户端响应数据
                    InetAddress clientAddress = packet.getAddress();
                    int clientPort = packet.getPort();
                    byte[] response = "here".getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(response, response.length, clientAddress, clientPort);

                    // 响应客户端
                    send(socket, responsePacket, Config.lossRate);
                }
            } catch (IOException e) {
                System.out.println("[error]:HeartbeatReceiver failed to receive");
                e.printStackTrace();
            }
        }
    }
}

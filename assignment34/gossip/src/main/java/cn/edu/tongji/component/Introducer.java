package cn.edu.tongji.component;

import cn.edu.tongji.config.Config;
import cn.edu.tongji.entity.Member;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Introducer {
    private static final MemberList memberList = new MemberList();

    public static void addMember(String address) {
        long timestamp = System.currentTimeMillis();
        memberList.join(String.valueOf(timestamp), address);
        spreadNewMember(String.valueOf(timestamp), address);
        System.out.println("[Introducer]: " + address + " 加入组！");
    }

    static class RunnableBroadCast implements Runnable {
        private final String timeStamp;
        private final String destAddress;
        private Thread thread;

        RunnableBroadCast(String timeStamp, String address, String destAddress) {
            this.destAddress = destAddress;
            this.timeStamp = timeStamp;
        }

        public void start() {
            if (thread == null) {
                thread = new Thread(this);
                thread.start();
            }
        }

        public void sendMessage() throws UnknownHostException {
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress address = InetAddress.getByName(destAddress);
                byte[] data = (timeStamp + " " + address).getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, address, Config.broadcastPort);

                socket.send(packet);
                System.out.println("[Introducer]: 成功向 " + destAddress + " 发送新成员信息！");

                byte[] responseData = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length);

                socket.setSoTimeout(5000);
                socket.receive(responsePacket);

                String reply = new String(responseData, 0, responsePacket.getLength());
                System.out.println("[Introducer]: 从 " + destAddress + " 获取到 reply: " + reply);
            } catch (IOException e) {
                System.out.println("[IntroducerError]: 无法向 " + destAddress + " 发送新成员加入消息！");
            }
        }

        @Override
        public void run() {
            try {
                sendMessage();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    static class ReceiveChange implements Runnable {
        @Override
        public void run() {
            while (true) {
                try (DatagramSocket socket = new DatagramSocket(Config.introducerListPort)) {
                    byte[] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);

                    socket.receive(packet);
                    String info = new String(data, 0, packet.getLength());

                    String[] words = info.trim().split("\\s+");

                    if (words.length != 4 || !words[0].equals("gossip")) {
                        System.out.println("[IntroducerError]: 消息格式无效！");
                        continue;
                    }

                    if (words[2].equals("failure") || words[2].equals("leave")) {
                        memberList.remove(words[3]);
                    }
                } catch (IOException e) {
                    System.out.println("[IntroducerError]: 监听列表变化出现故障！");
                }
            }
        }
    }

    private static void spreadNewMember(String timeStamp, String address) {
        for (Member member : MemberList.members) {
            RunnableBroadCast r = new RunnableBroadCast(timeStamp, address, member.getAddress());
            r.start();
        }
    }

    public static void main(String[] args) {
        Thread listenListThread = new Thread(new ReceiveChange());
        listenListThread.start();
        while (true) {
            try (DatagramSocket socket = new DatagramSocket(Config.introducerPort)) {
                byte[] data = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);

                System.out.println("[Introducer]: Introducer(" + Config.introducerAddress + ") has booted");
                socket.receive(packet);

                String info = new String(data, 0, packet.getLength());
                System.out.println("[Introducer]: get message：" + info);

                InetAddress address = packet.getAddress();
                int port = packet.getPort();

                if (!info.equals("client")) {
                    addMember(info);
                }

                byte[] data2 = memberList.membersToString().getBytes();
                DatagramPacket packet2 = new DatagramPacket(data2, data2.length, address, port);

                socket.send(packet2);
            } catch (IOException e) {
                System.out.println("[Error]: 接收错误！");
                e.printStackTrace();
            }
        }
    }
}

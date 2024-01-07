package cn.edu.tongji.swim.lib;


import cn.edu.tongji.swim.netEvents.AckEvent;
import cn.edu.tongji.swim.netEvents.PingEvent;
import cn.edu.tongji.swim.netEvents.PingReqEvent;
import cn.edu.tongji.swim.membershipEvents.UpdateEvent;
import cn.edu.tongji.swim.messages.*;
import cn.edu.tongji.swim.netEvents.*;
import cn.edu.tongji.swim.options.UdpOptions;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static cn.edu.tongji.Main.*;

@Data
public class Net {
    @AllArgsConstructor
    @Data
    private static class Udp {
        int port;
        int maxDgramSize;
    }

    private Udp udp;
    private DatagramSocket udpSocket;
    private InetAddress localhost;
    private EventBus eventBus;
    private ExecutorService exec = Executors.newFixedThreadPool(1);

    public static final int MESSAGE_TYPE_SIZE = 1;
    public static final int LENGTH_SIZE = 2;

    public Net(UdpOptions udpOptions) {
        this.udp = new Udp(0, 0);
        this.udp.port = udpOptions.getPort();
        this.udp.maxDgramSize = udpOptions.getMaxDgramSize() == null ? 512 : udpOptions.getMaxDgramSize();
        this.eventBus = new EventBus();
    }

    public boolean listen() {
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.out.println("this machine is not online, net initialize failed");
            return false;
        }

        try {
            final InetAddress finalLocalhost = localhost;
            byte[] receiveData = new byte[udp.maxDgramSize];
            udpSocket = new DatagramSocket(udp.port);

            //开始监听事件
            ListeningEvent listeningEvent = new ListeningEvent(localhost, udp.port, udp.maxDgramSize);
            onListening(listeningEvent);

            exec.execute(() -> {
                try {
                    while (true) {
                        System.out.println("waiting...");
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        udpSocket.receive(receivePacket);

                        //接收消息会进行对应的事件触发
                        NetEvent.Rinfo rinfo = new NetEvent.Rinfo(receivePacket.getAddress().toString(), receivePacket.getPort());
                        MessageEvent messageEvent = new MessageEvent(receivePacket.getData(), rinfo);
                        onNetMessage(messageEvent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    //错误事件
                    ErrorEvent errorEvent = new ErrorEvent(finalLocalhost, udp.port, e.getMessage());
                    onError(errorEvent);
                }
            });

            return true;
        } catch (SocketException e) {
            e.printStackTrace();
            System.out.println(
                    "udp socket initialize failed\n" +
                    "on: " + localhost + '\n' +
                    "port: " + udp.port
            );
            return false;
        }
    }

    public void stop() {
        eventBus.unregister(this);
        udpSocket.close();
        exec.close();
    }

    public void onError(ErrorEvent event) {
        System.out.println(
                "udp socket error occur\n" +
                "on: " + event.getAddress() + '\n' +
                "port: " + event.getPort() + '\n' +
                "message: " + event.getMessage()
        );
    }

    public void onListening(ListeningEvent event) {
        System.out.println(
                "start listening\n" +
                "on: " + event.getAddress() + '\n' +
                "port: " + event.getPort() + '\n' +
                "maxDgramSize: " + event.getMaxDgramSize()
        );
    }

    public void onNetMessage(MessageEvent event) {
        System.out.println(
                localhost.getHostAddress() + ':' + udp.port + " received net message\n" +
                "from: " + event.getRinfo().format() + '\n' +
                "length: " + event.getBuffer().length
        );

        onMessage(event.getBuffer(), event.getRinfo());
    }

    public void onMessage(byte[] buffer, NetEvent.Rinfo rinfo) {
        // 读取第一个字节
        byte messageType = buffer[0];

        // 使用Arrays.copyOfRange将剩余的字节复制到新的数组中
        byte[] message = Arrays.copyOfRange(buffer, 1, buffer.length);

        switch (MessageType.values()[messageType]) {
            case COMPOUND -> onCompound(message, rinfo);
            case PING -> onPing(message, rinfo);
            case PING_REQ -> onPingReq(message, rinfo);
            case SYNC -> onSync(message, rinfo);
            case ACK -> onAck(message, rinfo);
            case UPDATE -> onUpdate(message, rinfo);
            default -> onUnknown(message, rinfo);
        }
    }

    public void onCompound(byte[] buffer, NetEvent.Rinfo rinfo) {
        System.out.println("received compound message");

        if (buffer.length < LENGTH_SIZE) {
            System.out.println("cannot parse number of messages in compound message");
            return;
        }

        ByteBuffer bf = ByteBuffer.wrap(Arrays.copyOfRange(buffer, 0, LENGTH_SIZE));
        int numberOfMessages = bf.getShort();
        System.out.println("number of messages: " + numberOfMessages);
        byte[] message = Arrays.copyOfRange(buffer, LENGTH_SIZE, buffer.length);
        int readIndex = LENGTH_SIZE;

        for (int i = 0; i < numberOfMessages; i++) {
            if (message.length - readIndex < LENGTH_SIZE) {
                System.out.println(
                        "cannot parse number of messages in compound message\n" +
                        "readIndex: " + readIndex + '\n' +
                        "from: " + rinfo.format() + '\n' +
                        "length: " + message.length + '\n' +
                        "buffer: " + Arrays.toString(message)
                );

                break;
            }

            byte[] messageLengthBinary = Arrays.copyOfRange(message, 0, LENGTH_SIZE);
            message = Arrays.copyOfRange(message, LENGTH_SIZE, message.length);
            readIndex += LENGTH_SIZE;
            int messageLength = ByteBuffer.wrap(messageLengthBinary).getShort();

            onMessage(Arrays.copyOfRange(message, 0, messageLength), rinfo);
            readIndex += messageLength;
            message = Arrays.copyOfRange(message, messageLength, message.length);
        }
    }

    public void onPing(byte[] buffer, NetEvent.Rinfo rinfo) {
        try {
            PingData data = swim.getCodec().decode(buffer, PingData.class);
            System.out.println(
                    "received ping message\n" +
                    "from: " + rinfo.format() + '\n' +
                    "data: " + data
            );

            eventBus.post(new PingEvent(data.getSeq(), rinfo.getAddress()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(
                    "failed to decode data\n" +
                    "from: " + rinfo.format() + '\n' +
                    "length: " + buffer.length + '\n' +
                    "buffer: " + Arrays.toString(buffer)
            );
        }
    }

    public void onPingReq(byte[] buffer, NetEvent.Rinfo rinfo) {
        try {
            PingReqData data = swim.getCodec().decode(buffer, PingReqData.class);
            System.out.println(
                    "received pingreq message\n" +
                    "from: " + rinfo.format() + '\n' +
                    "data: " + data
            );

            eventBus.post(new PingReqEvent(data.getSeq(), data.getDest(), rinfo.getAddress()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(
                    "failed to decode data\n" +
                    "from: " + rinfo.format() + '\n' +
                    "length: " + buffer.length + '\n' +
                    "buffer: " + Arrays.toString(buffer)
            );
        }
    }

    public void onSync(byte[] buffer, NetEvent.Rinfo rinfo) {
        try {
            SyncData data = swim.getCodec().decode(buffer, SyncData.class);
            System.out.println(
                    "received sync message\n" +
                    "from: " + rinfo.format() + '\n' +
                    "data: " + data
            );

            eventBus.post(new SyncEvent(data.getMember()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(
                    "failed to decode data\n" +
                    "from: " + rinfo.format() + '\n' +
                    "length: " + buffer.length + '\n' +
                    "buffer: " + Arrays.toString(buffer)
            );
        }
    }

    public void onAck(byte[] buffer, NetEvent.Rinfo rinfo) {
        try {
            AckData data = swim.getCodec().decode(buffer, AckData.class);
            System.out.println(
                    "received ack message\n" +
                    "from: " + rinfo.format() + '\n' +
                    "data: " + data
            );

            eventBus.post(new AckEvent(data.getSeq(), data.getHost()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(
                    "failed to decode data\n" +
                    "from: " + rinfo.format() + '\n' +
                    "length: " + buffer.length + '\n' +
                    "buffer: " + Arrays.toString(buffer)
            );
        }
    }

    public void onUpdate(byte[] buffer, NetEvent.Rinfo rinfo) {
        try {
            UpdateData data = swim.getCodec().decode(buffer, UpdateData.class);
            System.out.println(
                    "received update message\n" +
                    "from: " + rinfo.format() + '\n' +
                    "data: " + data
            );

            eventBus.post(new UpdateEvent(data.getMember()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(
                    "failed to decode data\n" +
                    "from: " + rinfo.format() + '\n' +
                    "length: " + buffer.length + '\n' +
                    "buffer: " + Arrays.toString(buffer)
            );
        }
    }

    public void onUnknown(byte[] buffer, NetEvent.Rinfo rinfo) {
        System.out.println(
                "received unknown buffer\n" +
                "from: " + rinfo.format() + '\n' +
                "buffer: " + Arrays.toString(buffer)
        );
        eventBus.post(new UnknownEvent(buffer, rinfo));
    }

    //外部调用
    public void sendMessages(String sender, List<Message> messages, String host) {
        int bytesAvailable = udp.maxDgramSize - MESSAGE_TYPE_SIZE - LENGTH_SIZE;
        List<byte[]> buffers = new ArrayList<>();

        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            byte typeBuffer = (byte) message.getType().ordinal();
            byte[] dataBuffer = null;

            try {
                switch (message.getType()) {
                    case ACK -> dataBuffer = swim.getCodec().encode(message.getData().getAckData());
                    case PING -> dataBuffer = swim.getCodec().encode(message.getData().getPingData());
                    case PING_REQ -> dataBuffer = swim.getCodec().encode(message.getData().getPingReqData());
                    case SYNC -> dataBuffer = swim.getCodec().encode(message.getData().getSyncData());
                    case UPDATE -> dataBuffer = swim.getCodec().encode(message.getData().getUpdateData());
                    default -> {
                        System.out.println("unknown message type, stop encoding");
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(
                        "failed to encode data\n" +
                        "to: " + host + '\n' +
                        "data: " + message.getData() + '\n'
                );
                return;
            }

            System.out.println(
                    sender + " sending multiple...\n" +
                    "from: " + localhost.getHostAddress() + ':' + udp.port + '\n' +
                    "to: " + host + '\n' +
                    "type: " + message.getType().toString() + '\n' +
                    "data: " + message.getData()
            );
            ByteBuffer bf = ByteBuffer.allocate(dataBuffer.length + 1);
            bf.put(typeBuffer);
            bf.put(dataBuffer);
            byte[] totalBuffer = bf.array();

            if (totalBuffer.length + LENGTH_SIZE < bytesAvailable) {
                System.out.println("add buffers");
                buffers.add(totalBuffer);
                bytesAvailable -= (totalBuffer.length + LENGTH_SIZE);
            }
            else if (buffers.size() == 0) {
                System.out.println(
                        "oversized message\n" +
                        "length: " + totalBuffer.length + '\n' +
                        "message: " + message
                );
            }
            else {
                sendBuffer(makeCompoundMessages(buffers), host);
                bytesAvailable = udp.maxDgramSize - LENGTH_SIZE;
                buffers.clear();
                i--;
            }
        }

        if (buffers.size() > 0) {
            System.out.println("end, make compound");
            sendBuffer(makeCompoundMessages(buffers), host);
        }
    }

    public void piggybackAndSend(byte[] buffer, String host) {
        int bytesAvailable = udp.maxDgramSize - MESSAGE_TYPE_SIZE - LENGTH_SIZE * 2 - buffer.length;

        try {
            List<byte[]> buffers = swim.getDisseminator().getUpdatesUpTo(bytesAvailable);

            if (buffers.size() == 0) {
                sendBuffer(buffer, host);
                return;
            }

            buffers.add(0, buffer);
            sendBuffer(makeCompoundMessages(buffers), host);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(
                    "failed to get updates up to\n" +
                    "to: " + host + '\n' +
                    "buffer: " + Arrays.toString(buffer) + '\n'
            );
        }
    }

    //外部调用
    public void sendMessage(String sender, Message message, String host) {
        if (message.getData() != null) {
            byte[] data = null;

            try {
                switch (message.getType()) {
                    case ACK -> data = swim.getCodec().encode(message.getData().getAckData());
                    case PING -> data = swim.getCodec().encode(message.getData().getPingData());
                    case PING_REQ -> data = swim.getCodec().encode(message.getData().getPingReqData());
                    case SYNC -> data = swim.getCodec().encode(message.getData().getSyncData());
                    case UPDATE -> data = swim.getCodec().encode(message.getData().getUpdateData());
                    default -> {
                        System.out.println("unknown message type, stop encoding");
                        return;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(
                        "failed to encode data\n" +
                        "to: " + host + '\n' +
                        "data: " + message.getData() + '\n'
                );
            }

            System.out.println(
                    sender + " sending single...\n" +
                    "from: " + localhost.getHostAddress() + ':' + udp.port + '\n' +
                    "to: " + host + '\n' +
                    "type: " + message.getType().toString() + '\n' +
                    "data: " + message.getData()
            );
            ByteBuffer bf = ByteBuffer.allocate(MESSAGE_TYPE_SIZE + data.length);
            bf.put((byte) message.getType().ordinal());
            bf.put(data);
            piggybackAndSend(bf.array(), host);
        }
        else {
            System.out.println(
                    sender + " sending single empty...\n" +
                    "from: " + localhost.getHostAddress() + ':' + udp.port + '\n' +
                    "to: " + host + '\n' +
                    "type: " + message.getType().toString() +
                    "data: " + message.getData()
            );
            ByteBuffer bf = ByteBuffer.allocate(MESSAGE_TYPE_SIZE);
            bf.put((byte) message.getType().ordinal());
            piggybackAndSend(bf.array(), host);
        }
    }

    public byte[] makeCompoundMessages(List<byte[]> buffers) {
        int compoundLength = MESSAGE_TYPE_SIZE + LENGTH_SIZE;

        for (byte[] buffer : buffers) {
            compoundLength += (LENGTH_SIZE + buffer.length);
        }

        ByteBuffer bf = ByteBuffer.allocate(compoundLength);
        bf.put((byte) MessageType.COMPOUND.ordinal());
        bf.putShort((short) buffers.size());

        for (byte[] buffer : buffers) {
            bf.putShort((short) buffer.length);
            bf.put(buffer);
        }

        return bf.array();
    }

    public void sendBuffer(byte[] buffer, String host) {
        String[] rinfo = host.split(":");
        final String address = rinfo[0];
        final int port = Integer.parseInt(rinfo[1]);

        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length, inetAddress, port);
            udpSocket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(
                    "failed to send buffer\n" +
                    "to: " + host + '\n' +
                    "length: " + buffer.length
            );
        }
    }
}

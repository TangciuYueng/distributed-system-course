//package cn.edu.tongji.swim;
//
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//
//import java.io.IOException;
//import java.net.*;
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//@EqualsAndHashCode(callSuper = true)
//@Data
//public class Net extends EventEmitter {
//    @AllArgsConstructor
//    @Data
//    public static class Udp {
//        int port;
//        String type;
//        int maxDgramSize;
//    }
//
//
//
//    public enum EventType {
//        Error,
//        Listening,
//        Ping,
//        PingReq,
//        Sync,
//        Ack,
//        Update,
//        Unknown
//    }
//
//    private Swim swim;
//    private Udp udp;
//    private DatagramSocket udpSocket;
//    private EventHandler errorListener = new EventHandler() {
//        @Override
//        public void handle(Object... args) {
//            emit("error", EventType.Error);
//        }
//    };
//    private EventHandler listeningListener = new EventHandler() {
//        @Override
//        public void handle(Object... args) {
//            emit("listening", EventType.Listening);
//        }
//    };
//    private EventHandler messageListener = new EventHandler() {
//        @Override
//        public void handle(Object... args) {
//            onNetMessage((byte[]) args[0], (Rinfo) args[1]);
//        }
//    };
//
//    public static final int MESSAGE_TYPE_SIZE = 1;
//    public static final int LENGTH_SIZE = 2;
//
//    public Net(Swim swim, int udpPort, String udpType, int maxDgramSize) {
//        this.swim = swim;
//        this.udp.port = udpPort;
//        this.udp.type = udpType;
//        this.udp.maxDgramSize = maxDgramSize;
//        this.udpSocket = createDatagramSocket(udpPort);
//        on("message", messageListener);
//    }
//
//    private DatagramSocket createDatagramSocket(final int udpPort) {
//        try {
//            return new DatagramSocket(udpPort);
//        } catch (SocketException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    public void onNetMessage(byte[] buffer, Rinfo rinfo) {
//        System.out.println(
//                "received buffer\n" +
//                "from: " + rinfo.format() + '\n' +
//                "length: " + buffer.length + '\n'
//        );
//
//        onMessage(buffer, rinfo);
//    }
//
//    public void onMessage(byte[] buffer, Rinfo rinfo) {
//        // 读取第一个字节
//        byte messageType = buffer[0];
//
//        // 使用Arrays.copyOfRange将剩余的字节复制到新的数组中
//        byte[] message = Arrays.copyOfRange(buffer, 1, buffer.length);
//
//        switch (MessageType.values()[messageType]) {
//            case COMPOUND -> onCompound(message, rinfo);
//            case PING -> onPing(message, rinfo);
//            case PING_REQ -> onPingRec(message, rinfo);
//            case SYNC -> onSync(message, rinfo);
//            case ACK -> onAck(message, rinfo);
//            case UPDATE -> onUpdate(message, rinfo);
//            default -> onUnknown(message, rinfo);
//        }
//    }
//
//    public void onCompound(byte[] buffer, Rinfo rinfo) {
//        System.out.println("received compound message");
//
//        if (buffer.length < LENGTH_SIZE) {
//            System.out.println("cannot parse number of messages in compound message");
//            return;
//        }
//
//        int numberOfMessages = buffer[0];
//        byte[] message = Arrays.copyOfRange(buffer, 1, buffer.length);
//        int readIndex = LENGTH_SIZE;
//
//        for (int i = 0; i < numberOfMessages; i++) {
//            if (message.length - readIndex < LENGTH_SIZE) {
//                System.out.println(
//                        "cannot parse number of messages in compound message\n" +
//                        "readIndex: " + readIndex + '\n' +
//                        "from: " + rinfo.format() + '\n' +
//                        "length: " + message.length + '\n' +
//                        "buffer: " + Arrays.toString(message)
//                );
//
//                break;
//            }
//
//            byte[] messageLengthBinary = Arrays.copyOfRange(message, 0, LENGTH_SIZE);
//            message = Arrays.copyOfRange(message, LENGTH_SIZE, message.length);
//            int messageLength = ByteBuffer.wrap(messageLengthBinary).getInt();
//            onMessage(Arrays.copyOfRange(message, readIndex, readIndex + messageLength), rinfo);
//            readIndex += messageLength;
//        }
//    }
//
//    public void onPing(byte[] buffer, Rinfo rinfo) {
//        try {
//            byte[] data = swim.getCodec().decode(buffer);
//            System.out.println(
//                    "received ping message\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "data: " + Arrays.toString(data)
//            );
//
//            emit("message: PING", data, rinfo.format());
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(
//                    "failed to decode data\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "length: " + buffer.length + '\n' +
//                    "buffer: " + Arrays.toString(buffer)
//            );
//        }
//    }
//
//    public void onPingRec(byte[] buffer, Rinfo rinfo) {
//        try {
//            byte[] data = swim.getCodec().decode(buffer);
//            System.out.println(
//                    "received pingreq message\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "data: " + Arrays.toString(data)
//            );
//
//            emit("message: PING_REQ", data, rinfo.format());
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(
//                    "failed to decode data\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "length: " + buffer.length + '\n' +
//                    "buffer: " + Arrays.toString(buffer)
//            );
//        }
//    }
//
//    public void onSync(byte[] buffer, Rinfo rinfo) {
//        try {
//            byte[] data = swim.getCodec().decode(buffer);
//            System.out.println(
//                    "received sync message\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "data: " + Arrays.toString(data)
//            );
//
//            emit("message: SYNC", data, rinfo.format());
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(
//                    "failed to decode data\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "length: " + buffer.length + '\n' +
//                    "buffer: " + Arrays.toString(buffer)
//            );
//        }
//    }
//
//    public void onAck(byte[] buffer, Rinfo rinfo) {
//        try {
//            byte[] data = swim.getCodec().decode(buffer);
//            System.out.println(
//                    "received ack message\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "data: " + Arrays.toString(data)
//            );
//
//            emit("message: ACK", data, rinfo.format());
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(
//                    "failed to decode data\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "length: " + buffer.length + '\n' +
//                    "buffer: " + Arrays.toString(buffer)
//            );
//        }
//    }
//
//    public void onUpdate(byte[] buffer, Rinfo rinfo) {
//        try {
//            byte[] data = swim.getCodec().decode(buffer);
//            System.out.println(
//                    "received update message\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "data: " + Arrays.toString(data)
//            );
//
//            emit("message: UPDATE", data, rinfo.format());
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(
//                    "failed to decode data\n" +
//                    "from: " + rinfo.format() + '\n' +
//                    "length: " + buffer.length + '\n' +
//                    "buffer: " + Arrays.toString(buffer)
//            );
//        }
//    }
//
//    public void onUnknown(byte[] buffer, Rinfo rinfo) {
//        System.out.println(
//                "received unknown buffer\n" +
//                "from: " + rinfo.format() + '\n' +
//                "buffer: " + Arrays.toString(buffer)
//        );
//
//        emit("message: UNKNOWN", buffer, rinfo.format());
//    }
//
//    public void sendMessages(List<Message> messages, String host) {
//        int bytesAvailable = udp.maxDgramSize - MESSAGE_TYPE_SIZE - LENGTH_SIZE;
//        List<byte[]> buffers = new ArrayList<>();
//
//        for (int i = 0; i < messages.size(); i++) {
//            Message message = messages.get(i);
//            byte typeBuffer = (byte) message.getType().ordinal();
//            byte[] dataBuffer = swim.getCodec().encode(message.getData());
//            ByteBuffer bf = ByteBuffer.allocate(dataBuffer.length + 1);
//            bf.put(typeBuffer);
//            bf.put(dataBuffer);
//            byte[] totalBuffer = bf.array();
//
//            if (totalBuffer.length + LENGTH_SIZE < bytesAvailable) {
//                buffers.add(totalBuffer);
//                bytesAvailable -= (totalBuffer.length + LENGTH_SIZE);
//            }
//            else if (buffers.size() == 0) {
//                System.out.println(
//                        "oversized message\n" +
//                        "length: " + totalBuffer.length + '\n' +
//                        "message: " + message
//                );
//            }
//            else {
//                sendBuffer(makeCompoundMessages(buffers), host);
//                bytesAvailable = udp.maxDgramSize - LENGTH_SIZE;
//                buffers.clear();
//                i--;
//            }
//        }
//
//        if (buffers.size() > 0) {
//            sendBuffer(makeCompoundMessages(buffers), host);
//        }
//    }
//
//    public void piggybackAndSend(byte[] buffer, String host) {
//        int bytesAvailable = udp.maxDgramSize - MESSAGE_TYPE_SIZE - LENGTH_SIZE * 2 - buffer.length;
//        List<byte[]> buffers = swim.getDisseminator().getUpdatesUpTo(bytesAvailable);
//
//        if (buffers.size() == 0) {
//            sendBuffer(buffer, host);
//            return;
//        }
//
//        buffers.add(0, buffer);
//        sendBuffer(makeCompoundMessages(buffers), host);
//    }
//
//    public void sendMessage(Message message, String host) {
//        if (message.getData() != null) {
//            byte[] data = swim.getCodec().encode(message.getData());
//            ByteBuffer bf = ByteBuffer.allocate(MESSAGE_TYPE_SIZE + data.length);
//            bf.put((byte) message.getType().ordinal());
//            bf.put(data);
//            piggybackAndSend(bf.array(), host);
//        }
//        else {
//            ByteBuffer bf = ByteBuffer.allocate(MESSAGE_TYPE_SIZE);
//            bf.put((byte) message.getType().ordinal());
//            piggybackAndSend(bf.array(), host);
//        }
//    }
//
//    public byte[] makeCompoundMessages(List<byte[]> buffers) {
//        int compoundLength = MESSAGE_TYPE_SIZE + LENGTH_SIZE;
//
//        for (byte[] buffer : buffers) {
//            compoundLength += (LENGTH_SIZE + buffer.length);
//        }
//
//        ByteBuffer bf = ByteBuffer.allocate(compoundLength);
//        bf.put((byte) MessageType.COMPOUND.ordinal());
//        bf.putShort((short) buffers.size());
//
//        for (byte[] buffer : buffers) {
//            bf.putShort((short) buffer.length);
//            bf.put(buffer);
//        }
//
//        return bf.array();
//    }
//
//    public void sendBuffer(byte[] buffer, String host) {
//        String[] rinfo = host.split(":");
//        final String address = rinfo[0];
//        final int port = Integer.parseInt(rinfo[1]);
//
//        try {
//            InetAddress inetAddress = InetAddress.getByName(address);
//            DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length, inetAddress, port);
//            udpSocket.send(packet);
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println(
//                    "failed to send buffer\n" +
//                    "to: " + host + '\n' +
//                    "length: " + buffer.length
//            );
//        }
//    }
//}

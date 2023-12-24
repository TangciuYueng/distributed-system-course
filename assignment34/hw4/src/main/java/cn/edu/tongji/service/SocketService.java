package cn.edu.tongji.service;

import cn.edu.tongji.gossip.Node;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketService {
    private static final Logger LOGGER = Logger.getLogger(SocketService.class.getName());
    private DatagramSocket datagramSocket;
    private byte[] receiveBuffer = new byte[1024];
    private DatagramPacket datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

    public SocketService(int port) throws SocketException {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, "Error creating DatagramSocket", e);
        }
        datagramSocket = socket;
    }

    public void sendGossip(Node node, Node message) {
        byte[] messageBytes = getMessageBytes(message);
        if (messageBytes != null) {
            sendGossipMessage(node, messageBytes);
        }
    }

    private void sendGossipMessage(Node node, byte[] messageBytes) {
        if (datagramSocket != null) {
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length,
                    node.getInetAddress(), node.getPort());
            try {
                datagramSocket.send(packet);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error sending gossip message", e);
            }
        }
    }

    private byte[] getMessageBytes(Node message) {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        try (ObjectOutput oo = new ObjectOutputStream(bStream)) {
            oo.writeObject(message);
            return bStream.toByteArray();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error converting message to bytes", e);
            return null;
        }
    }

    public Node receiveGossip() {
       if (datagramSocket != null) {
            try {
                datagramSocket.receive(datagramPacket);
                try (ObjectInputStream objectInputStream =
                             new ObjectInputStream(new ByteArrayInputStream(datagramPacket.getData()))) {
                    Node message = (Node) objectInputStream.readObject();
                    LOGGER.log(Level.INFO, "Received gossip message from {0}", message.getId());
                    return message;
                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.SEVERE, "Error reading gossip message", e);
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error receiving gossip message", e);
            }
        }
        return null;
    }

}

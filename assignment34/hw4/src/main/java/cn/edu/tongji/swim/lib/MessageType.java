package cn.edu.tongji.swim.lib;

public enum MessageType {
    COMPOUND(0),
    COMPRESSED(1),
    ENCRYPTED(2),
    PING(10),
    PING_REQ(11),
    SYNC(12),
    ACK(13),
    UPDATE(14);

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageType fromValue(int value) {
        for (MessageType messageType : values()) {
            if (messageType.value == value) {
                return messageType;
            }
        }
        throw new IllegalArgumentException("Invalid MessageType value: " + value);
    }
}

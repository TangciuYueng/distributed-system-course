package cn.edu.tongji.swim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.MessagePack;

import java.io.IOException;

public class Codec {
    private final String codec;

    public Codec() {
        this(Codec.Default.codec);
    }

    public Codec(String codec) {
        assertSupportedCodec(codec);
        this.codec = codec;
    }

    public byte[] encode(Object obj) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        if ("json".equals(codec)) {
            return objectMapper.writeValueAsBytes(obj);
        } else {
            // Assume msgpack codec
            return MessagePack.pack(obj);
        }
    }

    public <T> T decode(byte[] buffer, Class<T> valueType) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        if ("json".equals(codec)) {
            return objectMapper.readValue(buffer, valueType);
        } else {
            // Assume msgpack codec
            return MessagePack.unpack(buffer, valueType);
        }
    }

    private void assertSupportedCodec(String codec) {
        assert ("json".equals(codec) || "msgpack".equals(codec)) : "unsupported codec";
    }

    public static class Default {
        public static final String codec = "msgpack";
    }
}

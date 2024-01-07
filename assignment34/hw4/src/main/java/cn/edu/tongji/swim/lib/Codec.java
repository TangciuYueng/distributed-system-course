package cn.edu.tongji.swim.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.msgpack.MessagePack;

import java.io.IOException;

public class Codec {
    private final String codec;

    // 无参构造函数　调用默认编码策略
    public Codec() {
        this(Codec.Default.CODEC);
    }

    /**
    * 构造方法，创建一个指定编码策略的编解码器对象
     * @param codec 要使用的编码策略，只能是"json"或"msgpack"
    * @throws IllegalArgumentException 如果编码策略不受支持
    */
    public Codec(String codec) {
        // 只能在 json 和 msgpack 中选择
        if (!isSupportedCodec(codec)) {
            throw new IllegalArgumentException("Unsupported codec");
        }
        this.codec = codec;
    }

    /**
     * 检查给定的编码策略是否受支持
     * @param codec 要检查的编码策略
     * @return 如果编码策略受支持则返回true，否则返回false
     */
    private boolean isSupportedCodec(String codec) {
        return "json".equals(codec) || "msgpack".equals(codec);
    }

    /**
     * 将给定参数对象编码
     *
     * @param obj 需要编码的 Java 对象
     * @return 编码后的字节数组
     */
    public byte[] encode(Object obj) throws IOException {
        if ("json".equals(codec)) {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsBytes(obj);
        } else {
            // 剩下默认为 msgpack 编码
            return MessagePack.pack(obj);
        }
    }

    /**
     * 解码方法，将字节数组解码为指定类型的对象
     * @param buffer 要解码的字节数组
     * @param valueType 要解码成的对象类型
     * @return 解码后的对象
     * @throws IOException 如果解码过程中发生I/O错误
     */
    public <T> T decode(byte[] buffer, Class<T> valueType) throws IOException {
        if ("json".equals(codec)) {
            ObjectMapper objectMapper = ObjectMapperFactory.createObjectMapper();
            return objectMapper.readValue(buffer, valueType);
        } else {
            // Assume msgpack codec
            return MessagePack.unpack(buffer, valueType);
        }
    }

    /**
     * 检查给定的编解码器是否受支持
     * @param codec 要检查的编解码器
     * @throws AssertionError 如果编解码器不受支持
     */
    private void assertSupportedCodec(String codec) {
        assert ("json".equals(codec) || "msgpack".equals(codec)) : "unsupported codec";
    }

    public interface Default {
        String CODEC = "msgpack";
    }

    class ObjectMapperFactory {
        public static ObjectMapper createObjectMapper() {
            return new ObjectMapper();
        }
    }
}

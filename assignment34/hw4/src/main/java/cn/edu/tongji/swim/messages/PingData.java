package cn.edu.tongji.swim.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.msgpack.annotation.Message;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Message
public class PingData {
    private int seq;
}

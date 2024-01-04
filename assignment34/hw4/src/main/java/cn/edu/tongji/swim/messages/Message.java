package cn.edu.tongji.swim.messages;

import cn.edu.tongji.swim.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
    private MessageType type;
    private Object data;
}

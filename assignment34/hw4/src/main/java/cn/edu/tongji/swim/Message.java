package cn.edu.tongji.swim;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
    private MessageType type;
    private Member data;
}

package cn.edu.tongji.swim.messages;

import cn.edu.tongji.swim.lib.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.msgpack.annotation.Message;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Message
public class UpdateData {
    private Member member;
}

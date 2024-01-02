package cn.edu.tongji.swim.membershpEvents;

import cn.edu.tongji.swim.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MembershipEvent {
    private Member data;
    private String host;
}

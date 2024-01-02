package cn.edu.tongji.swim.membershipEvents;

import cn.edu.tongji.swim.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChangeEvent {
    private Member data;
}

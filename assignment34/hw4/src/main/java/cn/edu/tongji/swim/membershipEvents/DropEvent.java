package cn.edu.tongji.swim.membershipEvents;

import cn.edu.tongji.swim.lib.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DropEvent {
    private Member data;
}

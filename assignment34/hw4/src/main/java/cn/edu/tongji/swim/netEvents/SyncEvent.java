package cn.edu.tongji.swim.netEvents;

import cn.edu.tongji.swim.lib.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncEvent {
    private Member member;
}

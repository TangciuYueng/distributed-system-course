package cn.edu.tongji.swim.failureDetectorEvents;

import cn.edu.tongji.swim.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuspectEvent {
    private Member member;
}

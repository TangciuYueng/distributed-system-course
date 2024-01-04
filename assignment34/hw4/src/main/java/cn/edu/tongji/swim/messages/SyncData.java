package cn.edu.tongji.swim.messages;

import cn.edu.tongji.swim.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncData {
    private Member member;
}

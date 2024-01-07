package cn.edu.tongji.swim.messages;

import cn.edu.tongji.swim.lib.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateData {
    private Member member;
}

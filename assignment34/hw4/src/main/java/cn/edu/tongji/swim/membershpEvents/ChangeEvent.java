package cn.edu.tongji.swim.membershpEvents;

import cn.edu.tongji.swim.Member;

public class ChangeEvent extends MembershipEvent {
    public ChangeEvent(Member data, String host) {
        super(data, host);
    }
}

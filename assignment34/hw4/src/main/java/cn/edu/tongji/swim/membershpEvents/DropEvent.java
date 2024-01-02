package cn.edu.tongji.swim.membershpEvents;

import cn.edu.tongji.swim.Member;

public class DropEvent extends MembershipEvent {
    public DropEvent(Member data, String host) {
        super(data, host);
    }
}

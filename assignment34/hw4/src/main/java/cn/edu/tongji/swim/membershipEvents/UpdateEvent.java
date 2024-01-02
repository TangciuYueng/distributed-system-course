package cn.edu.tongji.swim.membershipEvents;

import cn.edu.tongji.swim.Member;

public class UpdateEvent extends MembershipEvent {
    public UpdateEvent(Member data, String host) {
        super(data, host);
    }
}

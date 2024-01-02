package cn.edu.tongji.swim.failureDetectorEvents;

import cn.edu.tongji.swim.Member;

public class SuspectEvent extends FailureDetectorEvent {
    public SuspectEvent(Member member, String host) {
        super(member, host);
    }
}

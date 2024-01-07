package cn.edu.tongji.component;

import cn.edu.tongji.entity.Member;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class MemberList {
    public static List<Member> members = new ArrayList<>();

    public void show() {
        for (Member member : members) {
            System.out.println(member.getTimestamp() + ": " + member.getAddress());
        }
    }

    public void join(String timestamp, String address) {
        Member newMember = new Member(timestamp, address);
        for (Member member : members) {
            if (member.getAddress().equals(address)) {
                member.setTimestamp(timestamp);
                return;
            }
        }
        members.add(newMember);
    }

    public void remove(String address) {
        members.removeIf(member -> member.getAddress().equals(address));
    }

    public String nextServer(String address) {
        if (members.isEmpty()) {
            return null; // 处理空列表情况
        }

        int index = -1;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getAddress().equals(address)) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            return null; // 处理传入的ip不存在于列表中的情况
        }

        index = (index + 1) % members.size();
        return members.get(index).getAddress();
    }

    public String lastServer(String address) {
        if (members.isEmpty()) {
            return null; // 处理空列表情况
        }

        int index = -1;
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).getAddress().equals(address)) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            return null; // 处理传入的 ip 不存在于列表中的情况
        }

        // 优化查找逻辑
        index = (index - 1 + members.size()) % members.size();
        return members.get(index).getAddress();
    }

    public String membersToString() {
        StringBuffer sb = new StringBuffer();
        for (Member member:members){
            sb.append(member.getTimestamp() + " " + member.getAddress() + "\n");
        }
        return sb.toString();
    }
}
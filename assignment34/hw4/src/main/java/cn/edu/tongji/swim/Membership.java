package cn.edu.tongji.swim;

import org.msgpack.annotation.Message;

import java.lang.management.MemoryType;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Membership {
    public interface MembershipDefault {
        int suspectTimeout = 10;
        boolean preferCurrentMeta = false;
    }

    public enum EventType {
        CHANGE, DROP, UPDATE
    }

    private Swim swim;
    private Member local;
    private int suspectTimeout;
    private boolean preferCurrentMeta;
    private Map<String, Member> hostToMember;
    private Map<String, Member> hostToFaulty;
    private Map<String, Long> hostToSuspectTimeout;
    private ScheduledExecutorService scheduler;

    // 构造函数
    public Membership(Swim swim, Member local) {
        this(swim, local, MembershipDefault.suspectTimeout, MembershipDefault.preferCurrentMeta);
    }
    
    public Membership(Swim swim, Member local, int suspectTimeout, boolean preferCurrentMeta) {
        this.swim = swim;
        this.local = local;
        this.suspectTimeout = suspectTimeout;
        this.preferCurrentMeta = preferCurrentMeta;
        this.hostToMember = new ConcurrentHashMap<>();
        this.hostToFaulty = new ConcurrentHashMap<>();
        this.hostToSuspectTimeout = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);

        start();
    }

    private void start() {
        swim.getNet().addListener(FailureDetector.EventType.Suspect, this::onSuspect);
        swim.getNet().addListener(Net.EventType.Ack, this::onAck);
        swim.getNet().addListener(Net.EventType.Sync, this::onSync);
        swim.getNet().addListener(Net.EventType.Update, this::onUpdate);

        hostToSuspectTimeout.forEach((host, timeout) ->
            onSuspect(hostToMember.get(host))
        );
    }

    public void stop() {
        swim.getFailureDetector().removeSuspectListener(suspectListener);
        swim.getNet().removeAckListener(ackListener);
        swim.getNet().removeSyncListener(syncListener);
        swim.getNet().removeUpdateListener(updateListener);

        hostToSuspectTimeout.keySet().forEach(host -> {
//            ScheduledFuture<?> future = hostToSuspectTimeout.remove(host);
//            if (future != null) {
//                future.cancel(false);
//            }
        });
    }

    private void onSuspect(Member member) {
        /*
        var self = this;
        var data;

        member = new Member(member.data());
        member.state = Member.State.Suspect;
        data = member.data();
        */

        // 获得副本
        Member copy = member.getCopy();
        // 将设置为 SUSPECT
        copy.setState(Member.State.SUSPECT);
        Member copy2 = copy.getCopy();

        TimerTask setFaultyTask = new TimerTask() {
            @Override
            public void run() {
                hostToSuspectTimeout.remove(copy2.getHost());
                copy2.setState(Member.State.FAULTY);
                onUpdate(copy2);
            }
        };
        Timer timer = new Timer();
        timer.schedule(setFaultyTask, suspectTimeout);

        TimerTask updateTask = new TimerTask() {
            @Override
            public void run() {
                hostToSuspectTimeout.remove(copy2.getHost());
                onUpdate(copy.getCopy());
            }
        };

        timer.schedule(updateTask, 0);
    }

    private void onAck(String host) {
        Member member = hostToMember.getOrDefault(host, null);
        if (member != null && member.getState() == Member.State.SUSPECT) {
            swim.getNet().sendMessage(new Message(MessageType.UPDATE, member.getCopy()), host);
        }
    }

    private void onSync(Member member) {
        String host = member.getHost();

        // 获取本地和 faulty 的所有
        List<Member> allMembers = all(true, true);
        List<Message> messages = new ArrayList<>();

        for (Member data : allMembers) {
            Message message = new Message(MessageType.UPDATE, data);
            messages.add(message);
        }

        swim.getNet().sendMessages(messages, host);
    }

    private void sync(List<String> hosts) {
        List<Message> messages = List.of(new Message(MessageType.SYNC, local.getCopy()));

        all(false, true).forEach(member -> {
            messages.add(MessageType.UPDATE, member);
        });

        hosts.forEach(host ->
            swim.getNet().sendMessages(messages, host)
        );
    }

    private void onUpdate(Member member) {
        System.out.println("received update" + member);

        if (member.getState() == Member.State.ALIVE) {
            updateAlive(member);
        } else if (member.getState() == Member.State.SUSPECT) {
            updateSuspect(member);
        } else if (member.getState() == Member.State.FAULTY) {
            updateFaulty(member);
        }
    }

    void updateAlive(Member member) {

    }

    void updateSuspect(Member member) {

    }

    void updateFaulty(Member member) {

    }

    Member next() {

    }

    List<Member> random (int n) {

    }

    void shuffle() {}

    private int size(boolean hasLocal) {
        int count = hostToMember.size();
        if (hasLocal) {
            count++;
        }
        return count;
    }
    /**
     * 获取所有成员数据的列表
     *
     * @param hasLocal  如果为 true，则包含本地成员
     * @param hasFaulty 如果为 true，则包含故障成员
     * @return 所有成员数据的列表
     */
    private List<Member> all(boolean hasLocal, boolean hasFaulty) {
         List<Member> results = new ArrayList<>(hostToMember.size() + (hasLocal ? 1 : 0) + (hasFaulty ? hostToFaulty.size() : 0));

        // 添加存活成员到结果列表
        results.addAll(hostToMember.values());

        // 如果需要包含本地成员，则将本地成员添加到结果列表
        if (hasLocal) {
            results.add(local.getCopy());
        }

        // 如果需要包含故障成员，则将故障成员添加到结果列表
        if (hasFaulty) {
            results.addAll(hostToFaulty.values());
        }

        return results;
    }

    /**
     * 计算成员数据的校验和
     *
     * @return 校验和
     */
    public long checksum() {
        List<Member> members = all(true, false);

        // 按照端口号排序成员数据
        members.sort(Comparator.comparingInt(m -> Integer.parseInt(m.getHost().split(":")[1])));

        // 将排序后的成员数据转换成字符串列表
        List<String> memberStrings = members.stream()
                .map(member -> member.getHost() + member.getState() + member.getIncarnation())
                .collect(Collectors.toList());

        // 使用 farmhash 计算字符串列表的校验和
        return farmhash.hash64(String.join("-", memberStrings));
    }

    boolean isLocal(String host) {
        return host.equals(local.getHost());
    }

    void updateMeta(Object meta) {
        local.setMeta(meta);
        local.incarnate();
//        this.emit(Membership.EventType.Update, this.local.data());
    }
}

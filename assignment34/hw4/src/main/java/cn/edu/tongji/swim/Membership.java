package cn.edu.tongji.swim;

import cn.edu.tongji.swim.failureDetectorEvents.SuspectEvent;
import cn.edu.tongji.swim.membershipEvents.ChangeEvent;
import cn.edu.tongji.swim.membershipEvents.DropEvent;
import cn.edu.tongji.swim.membershipEvents.UpdateEvent;
import cn.edu.tongji.swim.messages.Message;
import cn.edu.tongji.swim.messages.SyncData;
import cn.edu.tongji.swim.messages.UpdateData;
import cn.edu.tongji.swim.netEvents.AckEvent;
import cn.edu.tongji.swim.netEvents.SyncEvent;
import lombok.Data;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Data
public class Membership {
    public interface MembershipDefault {
        int suspectTimeout = 10;
        boolean preferCurrentMeta = false;
    }

    private Swim swim;
    private Member local;
    private EventBus eventBus;
    private int suspectTimeout;
    private boolean preferCurrentMeta;
    private Map<String, Member> hostToMember;
    private Map<String, Member> hostToFaulty;
    private Map<String, Member> hostToIterable;
    private Map<String, ScheduledFuture<?>> hostToSuspectTimeout;
    private ScheduledExecutorService scheduler;

    // 构造函数
    public Membership(Swim swim, Member local) {
        this(swim, local, MembershipDefault.suspectTimeout, MembershipDefault.preferCurrentMeta);
    }

    public Membership(Swim swim, Member local, int suspectTimeout, boolean preferCurrentMeta) {
        this.swim = swim;
        this.local = local;
        this.eventBus = new EventBus();
        this.suspectTimeout = suspectTimeout;
        this.preferCurrentMeta = preferCurrentMeta;
        this.hostToMember = new ConcurrentHashMap<>();
        this.hostToFaulty = new ConcurrentHashMap<>();
        this.hostToIterable = new ConcurrentHashMap<>();
        this.hostToSuspectTimeout = new ConcurrentHashMap<>();
        this.scheduler = Executors.newScheduledThreadPool(1);

        start();
    }

    // 将当前对象注册为故障检测器的事件总线和网络事件总线的事件监听器
    public void start() {
        swim.getFailureDetector().getEventBus().register(this);
        swim.getNet().getEventBus().register(this);

        hostToSuspectTimeout.keySet().forEach(host -> {
            onSuspect(new SuspectEvent(hostToMember.getOrDefault(host, null)));
        });
    }

    // 解除注册
    public void stop() {
        swim.getFailureDetector().getEventBus().unregister(this);
        swim.getNet().getEventBus().unregister(this);
        scheduler.shutdown();
        // 取消设置的定时任务
//
//        public class ScheduledExecutorExample {
//            private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//            private ScheduledFuture<?> scheduledFuture;
//
//            public void setTime() {
//                // Schedule a task to be executed after a delay of 1000 milliseconds (1 second)
//                scheduledFuture = scheduler.schedule(() -> {
//                    System.out.println("Task executed after delay");
//                }, 1000, TimeUnit.MILLISECONDS);
//            }
//
//            public void cancelTime() {
//                // Cancel the scheduled task if it has not already been executed
//                if (scheduledFuture != null && !scheduledFuture.isDone()) {
//                    scheduledFuture.cancel(true);
//                    System.out.println("Task canceled");
//                }
//            }
//
//            public static void main(String[] args) {
//                ScheduledExecutorExample example = new ScheduledExecutorExample();
//
//                // Set the time and schedule the task
//                example.setTime();
//
//                // Sleep for a while to allow the scheduled task to execute (or be canceled)
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                // Cancel the scheduled task
//                example.cancelTime();
//
//                // Shutdown the executor service
//                example.scheduler.shutdown();
//            }
//        }
    }

    @Subscribe
    public void onAck(AckEvent event) {
        //这个函数会和FailureDetector.onAck同时触发
        if (event.getHost() == null)
            return;

        String host = event.getHost();
        Member member = hostToMember.getOrDefault(host, null);
        if (member != null && member.getState() == Member.State.SUSPECT) {
            UpdateData data = new UpdateData(member.getCopy());
            Message message = new Message(MessageType.UPDATE, data);
            swim.getNet().sendMessage(message, host);
        }
    }

    @Subscribe
    public void onSuspect(SuspectEvent event) {
        Member member = event.getMember();
        if (member == null) {
            return;
        }
        // 获得副本
        Member copy = member.getCopy();
        // 将设置为 SUSPECT
        copy.setState(Member.State.SUSPECT);
        Member copy2 = copy.getCopy();

        // 取消对应延时任务
        hostToSuspectTimeout.get(copy2.getHost()).cancel(true);

        // 设置为 FAULTY 的任务
        Runnable setFaultyTask = () -> {
            hostToSuspectTimeout.remove(copy2.getHost());
            copy2.setState(Member.State.FAULTY);
            onUpdate(new UpdateEvent(copy2));
        };

        // 更新状态的任务
        Runnable updateTask = () -> {
            hostToSuspectTimeout.remove(copy2.getHost());
            onUpdate(new UpdateEvent(copy.getCopy()));
        };

        // 延迟执行设置为 FAULTY 的任务
        scheduler.schedule(setFaultyTask, suspectTimeout, TimeUnit.MILLISECONDS);

        // 立即执行更新状态的任务
        scheduler.schedule(updateTask, 0, TimeUnit.MILLISECONDS);
    }

    @Subscribe
    private void onSync(SyncEvent event) {
        Member member = event.getMember();
        String host = member.getHost();

        updateAlive(member);

        // 获取本地和 faulty 的所有
        List<Member> allMembers = all(true, true);
        List<Message> messages = new ArrayList<>();

        for (Member data : allMembers) {
            Message message = new Message(MessageType.UPDATE, new UpdateData(data));
            messages.add(message);
        }

        // 发送多条 msg
        swim.getNet().sendMessages(messages, host);
    }

    private void sync(List<String> hosts) {
        SyncData syncData = new SyncData(local.getCopy());
        Message syncMessage = new Message(MessageType.SYNC, syncData);
        List<Message> messages = new ArrayList<>(List.of(syncMessage));

        all(false, true).forEach(member -> {
            UpdateData updateData = new UpdateData(member);
            Message updateMessage = new Message(MessageType.UPDATE, updateData);
            messages.add(updateMessage);
        });

        hosts.forEach(host ->
            swim.getNet().sendMessages(messages, host)
        );
    }

    @Subscribe
    private void onUpdate(UpdateEvent event) {
        Member member = event.getMember();

        System.out.println("received update" + member);

        switch (member.getState()) {
            case ALIVE -> updateAlive(member);
            case SUSPECT -> updateSuspect(member);
            case FAULTY -> updateFaulty(member);
        }
    }

    void updateAlive(Member data) {
        if (Objects.equals(data.getHost(), local.getHost())) {
            if (local.incarnate(data, false, preferCurrentMeta)) {
                eventBus.post(new UpdateEvent(local.getCopy()));
            } else {
                eventBus.post(new DropEvent(data));
            }
            return;
        }

        var member = hostToFaulty.getOrDefault(data.getHost(), null);
        if (member != null && member.getIncarnation() > data.getIncarnation()) {
            eventBus.post(new DropEvent(data));
            return;
        }

        var member1 = hostToMember.getOrDefault(data.getHost(), null);
        if (member1 == null || data.getIncarnation() > member1.getIncarnation()) {
            hostToSuspectTimeout.get(data.getHost()).cancel(true);
            hostToSuspectTimeout.remove(data.getHost());
            hostToFaulty.remove(data.getHost());

            hostToMember.put(data.getHost(), new Member(data));
            if (member1 == null) {
                hostToIterable.put(data.getHost(), hostToMember.get(data.getHost()));
                eventBus.post(new ChangeEvent(hostToMember.get(data.getHost()).getCopy()));
            }

            eventBus.post(new UpdateEvent(hostToMember.get(data.getHost()).getCopy()));
        } else {
            eventBus.post(new DropEvent(data));
        }
    }

    void updateSuspect(Member data) {
        if (Objects.equals(data.getHost(), local.getHost())) {
            eventBus.post(new DropEvent(data));
            local.incarnate(data, true, preferCurrentMeta);
            eventBus.post(new UpdateEvent(local.getCopy()));
            return;
        }

        var member = hostToFaulty.getOrDefault(data.getHost(), null);
        if (member != null && member.getIncarnation() >= data.getIncarnation()) {
            eventBus.post(new DropEvent(data));
            return;
        }

        var member1 = hostToMember.getOrDefault(data.getHost(), null);
        if (member1 == null ||
            data.getIncarnation() > member1.getIncarnation() ||
            data.getIncarnation() == member1.getIncarnation() &&
            member1.getState() == Member.State.ALIVE) {

            hostToFaulty.remove(data.getHost());

            hostToMember.put(data.getHost(), new Member(data));
            if (member1 == null) {
                hostToIterable.put(data.getHost(), hostToMember.get(data.getHost()));
                eventBus.post(new ChangeEvent(hostToMember.get(data.getHost()).getCopy()));
            }

            eventBus.post(new UpdateEvent(hostToMember.get(data.getHost()).getCopy()));
        } else {
            eventBus.post(new DropEvent(data));
        }
    }

    void updateFaulty(Member data) {
        if (Objects.equals(data.getHost(), local.getHost())) {
            eventBus.post(new DropEvent(data));
            local.incarnate(data, true, preferCurrentMeta);
            return;
        }

        var member = hostToMember.getOrDefault(data.getHost(), null);
        if (member != null && data.getIncarnation() >= member.getIncarnation()) {
            hostToFaulty.put(data.getHost(), new Member(data));
            hostToMember.remove(data.getHost());
            hostToIterable.remove(data.getHost());

            eventBus.post(new ChangeEvent(data));
            eventBus.post(new UpdateEvent(data));
        } else {
            eventBus.post(new DropEvent(data));
        }
    }

    Member next() {
        List<String> hosts = hostToIterable.keySet().stream().toList();
        String host;
        Member member;

        if (hosts.size() == 0) {
            shuffle();
            hosts = hostToIterable.keySet().stream().toList();
        }
        Random random = new Random();
        host = hosts.get(random.nextInt(hosts.size()));
        member = hostToIterable.get(host);
        hostToIterable.remove(host);

        return member;
    }

    List<String> random (int n) {
        List<String> hosts = hostToMember.keySet().stream().toList();
        List<String> selected = new ArrayList<>();
        int index, i;
        Random random = new Random();
        for (i = 0; i < n && i < hosts.size(); ++i) {
            index = i + random.nextInt(hosts.size() - i);
            selected.add(hosts.get(index));
        }

        return selected;
    }

    void shuffle() {
        hostToIterable = new ConcurrentHashMap<>();

        hostToMember.forEach((host, member) ->
            hostToIterable.put(host, member)
        );
    }

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
        local.incarnate(null, false, false);
//        this.emit(Membership.EventType.Update, this.local.data());
    }
}

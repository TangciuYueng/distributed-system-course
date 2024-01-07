package cn.edu.tongji.swim.lib;

import cn.edu.tongji.swim.netEvents.AckEvent;
import cn.edu.tongji.swim.netEvents.PingEvent;
import cn.edu.tongji.swim.netEvents.PingReqEvent;
import cn.edu.tongji.swim.failureDetectorEvents.SuspectEvent;
import cn.edu.tongji.swim.messages.*;
import cn.edu.tongji.swim.options.FDOptions;
import lombok.Data;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.edu.tongji.swim.lib.JsTime.setTimeout;

@Data
public class FailureDetector {

    private final Swim swim;
    private final int interval;
    private EventBus eventBus;
    private final int pingTimeout;
    private final int pingReqTimeout;
    private final int pingReqGroupSize;
    private int seq;
    private Timer tickTimer;
    private final ConcurrentMap<Integer, Timer> seqToTimeout = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, Runnable> seqToCallback = new ConcurrentHashMap<>();

    private static final int DEFAULT_INTERVAL = 20;
    private static final int DEFAULT_PING_TIMEOUT = 4;
    private static final int DEFAULT_PING_REQ_TIMEOUT = 12;
    private static final int DEFAULT_PING_REQ_GROUP_SIZE = 3;

    public static final String SUSPECT_EVENT = "suspect";
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // 构造函数
    public FailureDetector(Swim swim, FDOptions fdOptions) {
        this.swim = swim;
        this.eventBus = new EventBus();
        this.interval = fdOptions.getInterval() == null ? DEFAULT_INTERVAL : fdOptions.getInterval();
        this.pingTimeout = fdOptions.getPingTimeout() == null ? DEFAULT_PING_TIMEOUT : fdOptions.getPingTimeout();
        this.pingReqTimeout = fdOptions.getPingReqTimeout() == null ? DEFAULT_PING_REQ_TIMEOUT : fdOptions.getPingReqTimeout();
        this.pingReqGroupSize = fdOptions.getPingReqGroupSize() == null ? DEFAULT_PING_REQ_GROUP_SIZE : fdOptions.getPingReqGroupSize();
    }

    public void start() {
        this.swim.getNet().getEventBus().register(this);
        tick();
    }

    public void stop() {
        // 如果定时器不为null，则取消定时器
        if (tickTimer != null) {
            tickTimer.cancel();
            tickTimer = null;
        }

        // 移除网络事件监听器
        swim.getNet().getEventBus().unregister(this);

        // 清空存储定时器和回调的集合
        seqToTimeout.clear();
        seqToCallback.clear();
    }

    // 该方法的主要作用是在创建定时器并设定周期性任务，周期性地执行 ping 操作。
    private void tick() {
        // 创建定时任务，周期性执行 ping 操作
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ping();
            }
        };

        // 创建定时器并设定周期
        tickTimer = new Timer(true);
        tickTimer.scheduleAtFixedRate(timerTask, 0, interval);
    }

    private void ping() {
        pingMember(swim.getMembership().next());
    }

    private void pingMember(Member member) {
        // 获取当前序列号
        final int oldSeq = seq;

        // 递增序列号
        seq += 1;

        // 使用定时器，在超时后执行 receiveTimeout 操作
        Timer timer = new Timer();
        setTimeout(timer, () -> {
            clearSeq(oldSeq);
            pingReq(member);
        }, pingTimeout);
        seqToTimeout.put(oldSeq, timer);

        // 使用网络模块发送 Ping 消息
        PingData data = new PingData(seq);
        Message message = new Message(MessageType.PING, data);
        swim.getNet().sendMessage(message, member.getHost());
    }

    private void pingReq(Member member) {
        // 获取 pingReq 目标的随机成员列表
        List<String> relayMembers = swim.getMembership().random(pingReqGroupSize);

        // 如果随机成员列表为空，直接返回
        if (relayMembers.size() == 0) {
            return;
        }

        // 定义、启动超时定时器
        Timer timeout = new Timer();
        setTimeout(timeout, () -> {
            eventBus.post(new SuspectEvent(member));
        }, pingReqTimeout);

        // 遍历随机成员列表，执行 pingReqThroughMember 操作
        for (String relayMember : relayMembers) {
            // 清除超时定时器
            pingReqThroughMember(member.getHost(), relayMember, timeout::cancel);
        }
    }

    private void pingReqThroughMember(String memberHost, String relayMemberHost, Runnable callback) {
        // 获取当前序列号
        final int oldSeq = seq;

        // 递增序列号
        seq += 1;

        // 使用定时器，在超时后执行 receiveTimeout 操作
        Timer timer = new Timer();
        setTimeout(timer, () -> {
            clearSeq(oldSeq);
        }, pingReqTimeout);
        seqToTimeout.put(oldSeq, timer);

        // 使用回调函数，在收到 pingReqAck 时执行回调操作
        seqToCallback.put(oldSeq, () -> {
            clearSeq(oldSeq);
            callback.run();
        });

        // 使用网络模块发送 PingReq 消息
        PingReqData data = new PingReqData(oldSeq, memberHost);
        Message message = new Message(MessageType.PING_REQ, data);
        swim.getNet().sendMessage(message, relayMemberHost);
    }

    @Subscribe
    private void onPing(PingEvent event) {
        // 使用网络模块发送 Ack 消息
        AckData data = new AckData(event.getSeq(), event.getHost());
        Message message = new Message(MessageType.ACK, data);
        swim.getNet().sendMessage(message, event.getHost());
    }

    @Subscribe
    private void onPingReq(PingReqEvent event) {
        // 获取当前序列号
        final int oldSeq = seq;

        // 递增序列号
        seq += 1;

        // 使用定时器，在超时后执行 receiveTimeout 操作
        Timer timer = new Timer();
        setTimeout(timer, () -> {
            clearSeq(oldSeq);
        }, pingTimeout);
        seqToTimeout.put(oldSeq, timer);

        // 使用回调函数，在收到 pingReqAck 时执行回调操作
        seqToCallback.put(oldSeq, () -> {
            clearSeq(oldSeq);
            AckData data = new AckData(event.getSeq(), null);
            Message message = new Message(MessageType.ACK, data);
            swim.getNet().sendMessage(message, event.getHost());
        });

        // 使用网络模块发送 Ping 消息
        PingData data = new PingData(event.getSeq());
        Message message = new Message(MessageType.PING, data);
        swim.getNet().sendMessage(message, event.getDest());
    }

    @Subscribe
    private void onAck(AckEvent event) {
        //这个函数会和Membership.onAck同时触发
        if (event.getSeq() == null)
            return;

        // 获取回调函数
        Runnable callback = seqToCallback.get(event.getSeq());

        // 如果回调函数不为null，使用线程池提交任务，在下一个线程中执行回调
        if (callback != null) {
            executorService.submit(callback);
        }

        // 清除序列号相关的数据
        clearSeq(event.getSeq());
    }

    private void clearSeq(int seq) {
        seqToTimeout.remove(seq);
        seqToCallback.remove(seq);
    }
}

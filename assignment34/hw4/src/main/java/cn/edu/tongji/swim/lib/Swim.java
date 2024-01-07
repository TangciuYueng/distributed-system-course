package cn.edu.tongji.swim.lib;

import cn.edu.tongji.swim.membershipEvents.ChangeEvent;
import cn.edu.tongji.swim.membershipEvents.UpdateEvent;
import cn.edu.tongji.swim.options.FDOptions;
import cn.edu.tongji.swim.options.SwimOptions;
import cn.edu.tongji.swim.options.UdpOptions;
import lombok.Data;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.*;

import static cn.edu.tongji.swim.lib.JsTime.*;
import static cn.edu.tongji.Main.*;

@Data
public class Swim {
    private static final int DEFAULT_JOIN_TIMEOUT = 300;
    private static final int DEFAULT_JOIN_CHECK_INTERVAL = 50;
    private SwimOptions opts;
    private Codec codec;
    private Disseminator disseminator;
    private FailureDetector failureDetector;
    private Membership membership;
    private Net net;
    private SwimState state;
    private EventBus eventBus;
    private int joinTimeout;
    private int joinCheckInterval;
    private Timer joinTimer;

    public enum SwimState {
        STARTED, STOPPED
    }

    public Swim(SwimOptions opts) {
        this.opts = opts;
        this.eventBus = new EventBus();
        this.codec = new Codec(
                opts.getCodec()
        );
        this.disseminator = new Disseminator(
                opts.getDisseminationFactor()
        );
        this.failureDetector = new FailureDetector(new FDOptions(
                opts.getInterval(),
                opts.getPingTimeout(),
                opts.getPingReqTimeout(),
                opts.getPingReqGroupSize()
            )
        );
        this.membership = new Membership(
                new Member(opts.getLocal()),
                opts.getSuspectTimeout(),
                opts.getPreferCurrentMeta()
        );
        this.net = new Net(new UdpOptions(
                Integer.parseInt(opts.getLocal().split(":")[1]),
                opts.getUdp().getMaxDgramSize()
            )
        );
        this.state = SwimState.STOPPED;

        this.joinTimeout = opts.getJoinTimeout() != null ? opts.getJoinTimeout() : DEFAULT_JOIN_TIMEOUT;
        this.joinCheckInterval = opts.getJoinCheckInterval() != null ? opts.getJoinCheckInterval()
                : DEFAULT_JOIN_CHECK_INTERVAL;
    }

    @Subscribe
    public void onChange(ChangeEvent event) {
        eventBus.post(new cn.edu.tongji.swim.swimEvents.ChangeEvent());
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        eventBus.post(new cn.edu.tongji.swim.swimEvents.UpdateEvent());
    }

    public void bootstrap(List<String> hosts) {
        if (state != SwimState.STOPPED) {
            CustomExceptions.InvalidStateException err = new CustomExceptions.InvalidStateException("Invalid state: " + state + ", expected: STOPPED");
            onBootstrap(localhost(), err);
            return;
        }

        if (!net.listen()) {
            CustomExceptions.ListenFailedException err = new CustomExceptions.ListenFailedException("Host: " + opts.getLocal());
            onBootstrap(localhost(), err);
            return;
        }
        else {
            failureDetector.start();
            membership.start();
            disseminator.start();
            membership.getEventBus().register(this);
            state = SwimState.STARTED;
            join(hosts);
        }
    }

    public int checkJoin(List<String> hosts, Timer checker, Timer timeout) {
        int numberOfHostsResponded = 0;

        for (String host : hosts) {
            numberOfHostsResponded += (membership.get(host) == null ? 0 : 1);
        }

        if (numberOfHostsResponded > 1) {
            checker.cancel();
            timeout.cancel();
            onBootstrap(localhost(), null);
        }

        return numberOfHostsResponded;
    }

    public void join(List<String> hosts) {
        if (state != SwimState.STARTED) {
            CustomExceptions.InvalidStateException err = new CustomExceptions.InvalidStateException("Invalid state: " + state + ", expected: STARTED");
            onBootstrap(localhost(), err);
        }

        if (hosts == null || hosts.size() == 0) {
            onBootstrap(localhost(), null);
            return;
        }

        hosts = hosts.stream()
                .filter(host -> !Objects.equals(host, opts.getLocal()))
                .toList();

        membership.sync(hosts);
        Timer checker = new Timer();
        Timer timeout = new Timer();
        List<String> finalHosts = hosts;
        setTimeout(timeout, () -> {
            checker.cancel();

            int numberOfHostsResponded = checkJoin(finalHosts, checker, timeout);
            if (numberOfHostsResponded == 0) {
                CustomExceptions.JoinFailedException err = new CustomExceptions.JoinFailedException(
                        "local: " + localhost() + '\n' +
                        "hosts: " + finalHosts + '\n' +
                        "numberOfHostsResponded: " + numberOfHostsResponded + '\n' +
                        "timeout: " + joinTimeout
                );

                onBootstrap(localhost(), err);
            }
        }, joinTimeout);

        if (joinCheckInterval < joinTimeout) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    checkJoin(finalHosts, checker, timeout);
                }
            };

            checker.scheduleAtFixedRate(task, 0, joinCheckInterval);
        }
    }

    public void leave() {
        membership.getEventBus().unregister(this);
        disseminator.stop();
        membership.stop();
        failureDetector.stop();
        net.stop();
        state = SwimState.STOPPED;
    }

    public List<Member> members(boolean hasLocal, boolean hasFaulty) {
        return membership.all(hasLocal, hasFaulty);
    }

    public long checksum() {
        return membership.checksum();
    }

    public String localhost() {
        return membership == null ? opts.getLocal() : membership.localhost();
    }

    public String whoami() {
        return localhost();
    }

    public void updateMeta(Object meta) {
        membership.updateMeta(meta);
    }
}

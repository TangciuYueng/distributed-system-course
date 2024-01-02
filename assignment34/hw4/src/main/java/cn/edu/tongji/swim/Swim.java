package cn.edu.tongji.swim;

import lombok.Data;

import javax.security.auth.callback.Callback;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Data
public class Swim {
    private SwimOptions opts;
    private Codec codec;
    private Disseminator disseminator;
    private FailureDetector failureDetector;
    private Membership membership;
    private Net net;
    private SwimState state;
    private int joinTimeout;
    private int joinCheckInterval;
    private List<EventListener> changeListeners;
    private List<EventListener> updateListeners;
    private Timer joinTimer;

    public enum SwimEventType {
        CHANGE,
        UPDATE,
        ERROR,
        READY
    }
    public enum SwimState {
        STARTED, STOPPED
    }
    public interface SwimDefaultOptions {
        int DEFAULT_JOIN_TIMEOUT = 300;
        int DEFAULT_JOIN_CHECK_INTERVAL = 50;
    }

    public Swim(SwimOptions opts) {
        this.opts = opts;

        this.codec = new Codec(opts.getCodec());
//        this.disseminator = new Disseminator(opts.disseminationFactor, this, opts.local.host);
//        this.failureDetector = new FailureDetector(opts.interval, opts.pingTimeout, opts.pingReqTimeout,
//                opts.pingReqGroupSize, this, opts.local.host);
//        this.membership = new Membership(opts.local, opts.suspectTimeout, opts.preferCurrentMeta, this, opts.local.host);
//        this.net = new Net(new UdpOptions(opts.udp.type, Integer.parseInt(opts.local.host.split(":")[1]),
//                opts.udp.maxDgramSize), this, opts.local.host);
        this.state = SwimState.STOPPED;

        this.joinTimeout = opts.getJoinTimeout() != 0 ? opts.getJoinTimeout() : SwimDefaultOptions.DEFAULT_JOIN_TIMEOUT;
        this.joinCheckInterval = opts.getJoinCheckInterval() != 0 ? opts.getJoinCheckInterval()
                : SwimDefaultOptions.DEFAULT_JOIN_CHECK_INTERVAL;

//        this.changeListeners = new ArrayList<>();
//        this.updateListeners = new ArrayList<>();
    }

    public void bootstrap(List<String> host, Callback callback) {
        if (state != SwimState.STOPPED) {
            CustomExceptions.InvalidStateException err = new CustomExceptions.InvalidStateException("Invalid state: " + state + ", expected: STOPPED");
            handleError(err, callback);
            return;
        }
    }

    private void handleError(CustomExceptions.InvalidStateException err, Callback callback) {
        if (callback != null) {
//            callback.onError(err);
        } else {
            // 执行 SwimEventType.ERROR 对应的函数
        }

    }
}

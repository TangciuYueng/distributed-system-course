package cn.edu.tongji.swim.netEvents;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NetEvent {
    @AllArgsConstructor
    @Data
    public static class Rinfo {
        String address;
        int port;

        public String format() {
            return address + ':' + port;
        }
    }

    private byte[] buffer;
    private Rinfo rinfo;

    public class ErrorEvent extends NetEvent {
        public ErrorEvent(byte[] buffer, Rinfo rinfo) {
            super(buffer, rinfo);
        }
    }

    public class ListeningEvent extends NetEvent {
        public ListeningEvent(byte[] buffer, Rinfo rinfo) {
            super(buffer, rinfo);
        }
    }



    public class PingReqEvent extends NetEvent {
        public PingReqEvent(byte[] buffer, Rinfo rinfo) {
            super(buffer, rinfo);
        }
    }

    public class SyncEvent extends NetEvent {
        public SyncEvent(byte[] buffer, Rinfo rinfo) {
            super(buffer, rinfo);
        }
    }

    public class AckEvent extends NetEvent {
        public AckEvent(byte[] buffer, Rinfo rinfo) {
            super(buffer, rinfo);
        }
    }

    public class UpdateEvent extends NetEvent {
        public UpdateEvent(byte[] buffer, Rinfo rinfo) {
            super(buffer, rinfo);
        }
    }

    public class UnknownEvent extends NetEvent {
        public UnknownEvent(byte[] buffer, Rinfo rinfo) {
            super(buffer, rinfo);
        }
    }

}

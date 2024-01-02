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
}

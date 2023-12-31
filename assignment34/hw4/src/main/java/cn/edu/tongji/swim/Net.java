package cn.edu.tongji.swim;


import lombok.AllArgsConstructor;
import lombok.Data;

public class Net {
    @AllArgsConstructor
    @Data
    public static class Udp {
        int port;
        String type;
        int maxDiagramSize;
    }
}

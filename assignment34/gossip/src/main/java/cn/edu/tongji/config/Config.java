package cn.edu.tongji.config;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class Config {
    public static List<String> addresses = Arrays.asList("8.130.90.215", "8.130.89.193", "124.221.188.168", "122.51.113.192", "124.220.39.190", "124.221.224.31", "43.142.102.35", "8.130.173.131");
    public static String introducerAddress = "8.130.90.215";
    public static int introducerPort = 8100;
    public static int heartbeatPort = 8101;
    public static int gossipPort = 8102;
    public static int broadcastPort = 8103;
    public static int introducerListPort = 8304;
    public static int queryPort = 8305;
    public static double lossRate = 0;
    public static int errorLimit = 2;
}

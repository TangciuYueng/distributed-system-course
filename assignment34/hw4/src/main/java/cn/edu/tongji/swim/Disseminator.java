//package cn.edu.tongji.swim;
//
//import lombok.Data;
//
//import java.io.IOException;
//import java.util.*;
//
//public class Disseminator {
//    public class DisseminatorDefault {
//        static int disseminationFactor = 15;
//        static int disseminationLimit = 3;
//        public static DisseminationFormula disseminationFormula = (factor, size) ->
//                (int) Math.ceil(factor * Math.log(size + 1) / Math.log(10));
//    }
//    public interface DisseminationFormula {
//        int calculate(int factor, int size);
//    }
//
//    public class Update extends Member {
//        private int attempts;
//
//        public Update(Object meta, String host, State state, int incarnation) {
//            super(meta, host, state, incarnation);
//            this.attempts = 0;
//        }
//
//        public Update(String host) {
//            super(host);
//        }
//
//        public int getAttempts() {
//            return attempts;
//        }
//
//        public void setAttempts(int attempts) {
//            this.attempts = attempts;
//        }
//    }
//
//    private Swim swim;
//    private int disseminationFactor;
//    private int disseminationLimit;
//    private DisseminationFormula disseminationFormula;
//    private Map<Integer, Map<String, Update>> attemptsToUpdates;
//
//    private Map<String, Integer> hostToAttempts;
//
//    public Disseminator(Swim swim) {
//        this.swim = swim;
//        this.disseminationFactor = DisseminatorDefault.disseminationFactor;
//        this.disseminationLimit = DisseminatorDefault.disseminationLimit;
//        attemptsToUpdates = new HashMap<>();
//        hostToAttempts = new HashMap<>();
//    }
//
//    public void start() {
////        this.swim.membership.on(Membership.EventType.Change, this.changeListener);
////        this.swim.membership.on(Membership.EventType.Update, this.updateListener);
//        updateDisseminationLimit();
//    }
//
//    public void stop() {
////        this.swim.membership.removeListener(Membership.EventType.Change, this.changeListener);
////        this.swim.membership.removeListener(Membership.EventType.Update, this.updateListener);
//    }
//
//    public void onChange() {
//        updateDisseminationLimit();
//    }
//
//    public void updateDisseminationLimit() {
//        disseminationLimit = disseminationFormula.calculate(disseminationFactor, swim.getMembership().size(true));
//
//        Iterator<Integer> attemptsIterator = attemptsToUpdates.keySet().iterator();
//        while (attemptsIterator.hasNext()) {
//            Integer attempts = attemptsIterator.next();
//            // 如果尝试次数超过传播限制
//            if (attempts >= disseminationLimit) {
//                for (String host : attemptsToUpdates.get(attempts).keySet()) {
//                    hostToAttempts.remove(host);
//                }
//                attemptsIterator.remove(); // 使用迭代器的 remove 方法
//            }
//        }
//    }
//
//    public void onUpdate(Update data) {
//        var update = new Update(data.getMeta(), data.getHost(), data.getState(), data.getIncarnation());
//        removeUpdate(update);
//        addUpdate(update);
//    }
//
//    void addUpdate(Update update) {
//        if (update.getAttempts() >= disseminationLimit) {
//            return;
//        }
//
//        // 如果当前尝试次数的映射不存在，则创建一个新的映射
//        attemptsToUpdates.computeIfAbsent(update.getAttempts(), k -> new HashMap<>());
//
//        // 将更新添加到对应尝试次数的映射中
//        attemptsToUpdates.get(update.getAttempts()).put(update.getHost(), update);
//
//        // 记录更新的尝试次数
//        hostToAttempts.put(update.getHost(), update.getAttempts());
//
//    }
//
//
//    public void removeUpdate(Update update) {
//        // 获取更新的尝试次数
//        int attempts = hostToAttempts.getOrDefault(update.getHost(), -1);
//
//        // 如果尝试次数有效，则从映射中删除对应的更新
//        if (attempts >= 0) {
//            Map<String, Update> attemptsMap = attemptsToUpdates.get(attempts);
//            if (attemptsMap != null) {
//                attemptsMap.remove(update.getHost());
//            }
//        }
//
//        // 从主机到尝试次数的映射中删除对应主机的记录
//        hostToAttempts.remove(update.getHost());
//    }
//
//    public List<byte[]> getUpdatesUpTo(int bytesAvailable) throws IOException {
//        List<byte[]> buffers = new ArrayList<>();
//        List<Update> updates = new ArrayList<>();
//        Map<String, Update> hostToUpdates;
//        int attempts;
//        byte[] buffer;
//        Update update;
//
//        for (attempts = 0; attempts < disseminationLimit; attempts++) {
//            if (bytesAvailable <= Net.MessageTypeSize) {
//                break;
//            }
//
//            hostToUpdates = attemptsToUpdates.get(attempts);
//
//            if (hostToUpdates != null) {
//                for (String host : hostToUpdates.keySet()) {
//                    if (bytesAvailable <= Net.MessageTypeSize) {
//                        break;
//                    }
//
//                    update = hostToUpdates.get(host);
//                    buffer = serializeUpdate(update);
//
//                    if (buffer.length + Net.LengthSize <= bytesAvailable) {
//                        buffers.add(buffer);
//                        updates.add(update);
//                        removeUpdate(update);
//                        bytesAvailable -= buffer.length + Net.LengthSize;
//                    }
//                }
//            }
//        }
//
//        for (Update update1 : updates) {
//            update1.setAttempts(update1.getAttempts() + 1);
//            addUpdate(update1);
//        }
//
//        return buffers;
//    }
//
//    private byte[] serializeUpdate(Update update) throws IOException {
//        byte[] header = new byte[Net.MessageTypeSize];
//
//        Net.writeMessageType(header, MessageType.UPDATE, 0);
//
//        // 假设 MemberData 类有相应的方法来获取 meta、host、state 和 incarnation
//        byte[] payload = this.swim.getCodec().encode(new Update(
//                update.getMeta(),
//                update.getHost(),
//                update.getState(),
//                update.getIncarnation()
//        ));
//
//        return concatenateByteArrays(header, payload);
//    }
//
//    private byte[] concatenateByteArrays(byte[] header, byte[] payload) {
//        int length1 = header.length;
//        int length2 = payload.length;
//        byte[] result = Arrays.copyOf(header, length1 + length2);
//        System.arraycopy(payload, 0, result, length1, length2);
//
//        return result;
//    }
//}

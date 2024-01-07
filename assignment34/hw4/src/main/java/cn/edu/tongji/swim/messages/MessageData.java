package cn.edu.tongji.swim.messages;

import lombok.Builder;
import lombok.Data;
import org.msgpack.annotation.Message;

@Data
@Builder
@Message
public class MessageData {
    private AckData ackData;
    private PingData pingData;
    private PingReqData pingReqData;
    private SyncData syncData;
    private UpdateData updateData;
}

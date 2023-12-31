package cn.edu.tongji.swim;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Member {
    public enum State {
        ALIVE,
        SUSPECT,
        FAULTY
    }

    private Object meta;
    private String host;
    private State state;
    private int incarnation;

    public Member(String host) {
        this.host = host;
        this.state = State.ALIVE;
        this.incarnation = 0;
    }

    public Member getCopy() {
        return new Member(meta, host, state, incarnation);
    }

    public boolean incarnate(Member data, boolean force, boolean preferCurrentMeta) {
        if (data == null) {
            incarnation += 1;
            return true;
        }

        if (data.getIncarnation() > incarnation) {
            if (!preferCurrentMeta) {
                meta = data.getMeta();
            }
            incarnation = data.getIncarnation() + 1;
            return true;
        }

        if (data.getIncarnation() == incarnation && force) {
            incarnation = data.getIncarnation() + 1;
            return true;
        }

        return false;
    }
}

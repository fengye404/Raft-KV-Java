package top.fengye.raft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.fengye.biz.Command;
import top.fengye.biz.Key;
import top.fengye.biz.Value;
import top.fengye.rpc.grpc.Grpc;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: FengYe
 * @date: 2024/4/9 下午11:15
 * @description: RaftStateMachine
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaftStateMachine {
    private RaftNode raftNode;
    private Map<Key, Value> db = new HashMap<>();

    public RaftStateMachine(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    public void apply(Command command) {
        switch (command.getCommandType()) {
            case PUT:
                doPut(command);
                break;
            case DEL:
                doDel(command);
                break;
        }
    }

    public Grpc.CommandResponse doGet(Command command) {
        Key key = command.getKey();
        Value value = db.get(key);
        if (value != null) {
            return Grpc.CommandResponse.newBuilder().setSuccess(true).setResult(new String(value.getData())).build();
        } else {
            return Grpc.CommandResponse.newBuilder().setSuccess(true).setResult("no value").build();
        }
    }

    public void doPut(Command command) {
        Key key = command.getKey();
        Value value = command.getValue();
        db.put(key, value);
    }

    public void doDel(Command command) {
        db.remove(command.getKey());
    }
}

package top.fengye.raft;

import top.fengye.biz.Command;
import top.fengye.biz.Key;
import top.fengye.biz.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: FengYe
 * @date: 2024/4/9 下午11:15
 * @description: RaftStateMachine
 */
public class RaftStateMachine {
    private RaftNode raftNode;
    private Map<Key, Value> db = new HashMap<>();

    public void apply(Command command){

    }
}

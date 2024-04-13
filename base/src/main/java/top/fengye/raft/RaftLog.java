package top.fengye.raft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import top.fengye.biz.Command;
import top.fengye.rpc.grpc.BizParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: RaftLog
 */
public class RaftLog {

    private RaftNode raftNode;
    private RaftStateMachine raftStateMachine;
    private LinkedHashMap<Long, Entry> entries = new LinkedHashMap<>();
    private long indexCount = 1;
    private long commitIndex = 0L;
    /**
     * 小根堆作为优先队列，优先处理 index 小的 entry
     */
    private PriorityQueue<Entry> queue = new PriorityQueue<>(Comparator.comparing(Entry::getIndex));

    public Entry append(Command command) {
        Entry entry = new Entry(getNextLogIndex(), raftNode.getCurrentTerm(), command);
        entries.put(entry.getIndex(), entry);
        queue.add(entry);
        return entry;
    }

    public void append(List<Entry> entries) {
        entries.forEach(entry -> {
            this.entries.put(entry.getIndex(), entry);
            queue.add(entry);
        });
    }


    public void apply(long endIndex) {
        if (queue.isEmpty()) {
            return;
        }
        while (!queue.isEmpty() && queue.peek().getIndex() < endIndex) {
            Entry poll = queue.poll();
            raftStateMachine.apply(poll.getCommand());
            commitIndex++;
        }
    }

    /**
     * entry 在 log 中的 index 从 1 开始
     * 因为 0 需要预留给 preLogIndex 使用
     *
     * @return
     */
    public long getNextLogIndex() {
        return indexCount;
    }

    public long getTermByIndex(long index) {
        return entries.get(index).getTerm();
    }

    /**
     * 校验传入的 preLogIndex 和 preLogTerm 是否和自己的相同
     *
     * @param preLogIndex
     * @param preLogTerm
     * @return
     */
    public boolean checkPre(Long preLogIndex, Long preLogTerm) {
        long myPreLogIndex = getNextLogIndex() - 1;
        if (myPreLogIndex == preLogIndex && getTermByIndex(myPreLogIndex) == preLogTerm) {
            return true;
        }
        return false;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Entry {

        private long index;

        private long term;

        private Command command;

        public static Entry parse(BizParam.LogEntry entry) {
            Entry res = new Entry();
            res.setIndex(entry.getIndex());
            res.setTerm(entry.getTerm());
            res.setCommand(Command.parse(entry.getCommand()));
            return res;
        }

        public BizParam.LogEntry antiParse() {
            return BizParam.LogEntry
                    .newBuilder()
                    .setIndex(index)
                    .setTerm(term)
                    .setCommand(command.antiParse())
                    .build();
        }
    }
}

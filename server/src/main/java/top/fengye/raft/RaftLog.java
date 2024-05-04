package top.fengye.raft;

import io.vertx.core.Promise;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import top.fengye.biz.Command;
import top.fengye.rpc.grpc.BizParam;
import top.fengye.rpc.grpc.Grpc;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: RaftLog
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RaftLog {

    private RaftNode raftNode;
    private List<Entry> entries = new ArrayList<>();
    // 当前已经被 commit 的 index，即被过半接收的日志的 index
    private int commitIndex = 0;
    // 最后一个被 apply 的 index
    private int lastAppliedIndex = 0;
    /**
     * apply 事件回调队列，用于当某个 entry 被 apply 时，回调使用
     */
    private PriorityQueue<Pair<Integer, Runnable>> applyEventQueue = new PriorityQueue<>(Comparator.comparing(Pair::getLeft));

    {
        // 保留 index 为 0 的 log，以便 index 为 1 的 entry 取pre
        // 因此 log 的 index 从 1 开始
        entries.add(new Entry(-1, -1, null));
    }

    public RaftLog(RaftNode raftNode) {
        this.raftNode = raftNode;
    }

    public Entry append(Command command, Runnable runnable) {
        Entry entry = new Entry(getNextLogIndex(), raftNode.getCurrentTerm(), command);
        entries.add(entry);
        applyEventQueue.add(Pair.of(entry.index, runnable));
        return entry;
    }

    public void append(List<Entry> entries) {
        this.entries.addAll(entries);
    }

    public List<Entry> slice(int startIndex) {
        return entries.subList(startIndex, getNextLogIndex());
    }


    /**
     * apply (lastAppliedIndex, endIndex]
     *
     * @param endIndex
     */
    public void apply(int endIndex) {
        // 调用 stateMachine 执行 apply
        int lastApplied = lastAppliedIndex;
        for (int i = lastApplied + 1; i <= endIndex; i++) {
            Entry entry = entries.get(i);
            raftNode.getRaftStateMachine().apply(entry.command);
            lastAppliedIndex++;
        }
        // 执行回调
        while (!applyEventQueue.isEmpty() && lastAppliedIndex >= applyEventQueue.peek().getLeft()) {
            Pair<Integer, Runnable> poll = applyEventQueue.poll();
            if (null != poll) {
                poll.getRight().run();
            }
        }
    }

    /**
     * 由于初始化的时候添加了一个空的 entry，所以需要 -1
     *
     * @return
     */
    public int getCurrentLogIndex() {
        return entries.size() - 1;
    }

    public int getCurrentLogTerm() {
        return getTermByIndex(getCurrentLogIndex());
    }

    public int getNextLogIndex() {
        return getCurrentLogIndex() + 1;
    }

    public int getPreLogIndex() {
        return getCurrentLogIndex() - 1;
    }

    public int getTermByIndex(int index) {
        return entries.get(index).getTerm();
    }

    /**
     * 校验传入的 preLogIndex 和 preLogTerm 是否和自己的相同
     *
     * @param preLogIndex
     * @param preLogTerm
     * @return
     */
    public boolean checkPre(int preLogIndex, int preLogTerm) {
        int myPreLogIndex = getCurrentLogIndex();
        return myPreLogIndex == preLogIndex && getTermByIndex(myPreLogIndex) == preLogTerm;
    }

    public void reCalculateCommitIndex() {
        int newCommitIndex = mooreVoteCommitIndex();
        if (newCommitIndex > commitIndex) {
            commitIndex = newCommitIndex;
            apply(newCommitIndex);
        }
    }

    /**
     * 通过摩尔投票算法，根据 matchLogIndexMap 来计算出当前过半节点认可的 commitIndex
     *
     * @return
     */
    private int mooreVoteCommitIndex() {
        List<Integer> currentIndexList = new ArrayList<>(raftNode.getMatchLogIndexMap().values());
        currentIndexList.add(getCurrentLogIndex());
        int mooreCount = 0;
        int res = 0;
        // 第一次遍历：查找候选元素
        for (int i : currentIndexList) {
            if (mooreCount == 0) {
                res = i;
            }
            if (res == i) {
                mooreCount++;
            } else {
                mooreCount--;
            }
        }
        // 第二次遍历：验证候选元素是否过半
        mooreCount = 0;
        for (int num : currentIndexList) {
            if (num == res) {
                mooreCount++;
            }
        }

        if (mooreCount > currentIndexList.size() / 2) {
            return res;
        } else {
            return commitIndex;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Entry {

        private int index;

        private int term;

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

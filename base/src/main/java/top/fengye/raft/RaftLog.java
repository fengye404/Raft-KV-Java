package top.fengye.raft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;
import top.fengye.biz.Command;
import top.fengye.rpc.grpc.BizParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: RaftLog
 */
@Data
public class RaftLog {

    private RaftNode raftNode;
    private RaftStateMachine raftStateMachine;
    private List<Entry> entries = new ArrayList<>();
    // 当前已经被 commit 的 index，即被过半接收的日志的 index
    private int commitIndex = 0;
    // 最后一个被 apply 的 index
    private int lastAppliedIndex = 0;
    /**
     * apply 事件回调队列，用于当某个 entry 被 apply 时，回调使用
     */
    private PriorityQueue<Pair<Integer, FunctionalInterface>> applyEventQueue = new PriorityQueue<>(Comparator.comparing(Pair::getLeft));

    {
        // 保留 index 为 0 的 log，以便 index 为 1 的 entry 取pre
        // 因此 log 的 index 从 1 开始
        entries.set(0, new Entry(-1, -1, null));
    }


    public Entry append(Command command) {
        Entry entry = new Entry(getCurrentLogIndex() + 1, raftNode.getCurrentTerm(), command);
        entries.add(entry);
        return entry;
    }

    public void append(List<Entry> entries) {
        this.entries.addAll(entries);
    }

    public List<Entry> slice(int startIndex) {
        return entries.subList(startIndex, getCurrentLogIndex() + 1);
    }


    public void apply(int endIndex) {

    }

    public int getCurrentLogIndex() {
        return entries.size();
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
        int myPreLogIndex = getPreLogIndex();
        return myPreLogIndex == preLogIndex && getTermByIndex(myPreLogIndex) == preLogTerm;
    }

    public void reCalculateCommitIndex() {
        int newCommitIndex = mooreVoteCommitIndex();
        if (newCommitIndex > commitIndex) {
            apply(newCommitIndex);
            commitIndex = newCommitIndex;
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

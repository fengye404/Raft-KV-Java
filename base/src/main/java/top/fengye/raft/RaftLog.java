package top.fengye.raft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import top.fengye.biz.Command;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: RaftLog
 */
public class RaftLog {

    private RaftNode raftNode;
    private List<Entry> entries;
    private long commitIndex;
    /**
     * 小根堆作为优先队列，优先处理 index 小的 entry
     */
    private PriorityQueue<Entry> queue = new PriorityQueue<>(Comparator.comparing(Entry::getIndex));

    public void appendToQueue(List<Entry> entries){
        queue.addAll(entries);
    }


    @Data
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Entry{

        private long index;

        private long term;

        private Command command;
    }
}

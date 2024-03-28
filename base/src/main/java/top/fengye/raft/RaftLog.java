package top.fengye.raft;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import top.fengye.biz.Command;

import java.util.List;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: RaftLog
 */
public class RaftLog {

    private List<Entry> entries;

    private long commitIndex;


    @Data
    @AllArgsConstructor
    @Accessors(chain = true)
    static class Entry{

        private long index;

        private long term;

        private Command command;
    }
}

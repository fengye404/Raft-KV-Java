package top.fengye.raft;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.impl.VertxByteBufAllocator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author: FengYe
 * @date: 2024/5/5 上午3:23
 * @description: RaftFile
 */
@Slf4j
public class RaftFile {
    private RaftNode raftNode;
    private String logFileName;
    private String nodeFileName;
    private FileChannel logFile;
    private FileChannel nodeInfo;
    private Executor executor;


    public RaftFile(RaftNode raftNode) {
        this.raftNode = raftNode;
        this.logFileName = raftNode.getNodeId() + "-log";
        this.nodeFileName = raftNode.getNodeId() + "-node";
        executor = Executors.newSingleThreadExecutor();
        try {
            logFile = FileChannel.open(Path.of(logFileName),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.SYNC);
            nodeInfo = FileChannel.open(Path.of(nodeFileName),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.SYNC);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        raftNode.getVertx().setPeriodic(5000L, id -> {
            sync();
        });
    }

    public void recover() {
        try {
            FileChannel readFileChannel = FileChannel.open(Path.of(logFileName), StandardOpenOption.READ);
            ByteBuf byteBuf = VertxByteBufAllocator.POOLED_ALLOCATOR.buffer();
            byteBuf.writeBytes(readFileChannel, 0L, (int) readFileChannel.size());
            while (byteBuf.readableBytes() > 0) {
                raftNode.getRaftLog().getEntries().add(new RaftLog.Entry(byteBuf));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void write(List<RaftLog.Entry> entries) {
        for (RaftLog.Entry entry : entries) {
            executor.execute(() -> {
                try {
                    logFile.write(entry.toByteBuffer());
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            });
        }
    }

    public void write(RaftNode raftNode) {
        executor.execute(() -> {
            byte[] bytes = raftNode.getVotedFor().getBytes();
            ByteBuffer allocate = ByteBuffer.allocate(4 + 4 + bytes.length);
            allocate.putInt(raftNode.getCurrentTerm())
                    .putInt(bytes.length)
                    .put(bytes);
        });
    }

    /**
     * write 写入可能存在缓存，需要定时 sync 手动同步确保数据不会丢失
     */
    public void sync() {
        executor.execute(() -> {
            try {
                logFile.force(true);
                nodeInfo.force(true);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        });
    }
}

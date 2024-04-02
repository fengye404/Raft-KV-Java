package top.fengye.rpc;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import top.fengye.rpc.grpc.Grpc;

/**
 * @author: FengYe
 * @date: 2024/4/3 1:19
 * @description: RpcProxy RPC代理，RaftNode通过这个接口执行RPC相关操作，屏蔽底层具体实现
 */
public interface RpcProxy {
    /**
     * 装载 vertx 实例
     * @param vertx
     */
    void mountVertxInstance(Vertx vertx);

    /**
     * 启动 handler 监听处理
     * @param port
     */
    void startHandler(int port);

    Promise<Grpc.ApplyVoteResponse> applyVote(Grpc.ApplyVoteRequest request);

    Promise<Grpc.AppendEntriesResponse> appendEntries(Grpc.AppendEntriesRequest request);
}

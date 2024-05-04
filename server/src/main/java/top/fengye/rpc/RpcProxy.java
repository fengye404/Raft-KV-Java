package top.fengye.rpc;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import top.fengye.raft.RaftNode;
import top.fengye.rpc.grpc.Grpc;

/**
 * @author: FengYe
 * @date: 2024/4/3 1:19
 * @description: RpcProxy RPC代理，RaftNode通过这个接口执行RPC相关操作，屏蔽底层具体实现
 */
public interface RpcProxy {
    /**
     * 装载 vertx 和 rpc handler
     *
     * @param node
     */
    void mountHandler(RaftNode node);

    Future<Grpc.ApplyVoteResponse> applyVote(RpcAddress rpcAddress, Grpc.ApplyVoteRequest request);

    Future<Grpc.AppendEntriesResponse> appendEntries(RpcAddress rpcAddress, Grpc.AppendEntriesRequest request);
}

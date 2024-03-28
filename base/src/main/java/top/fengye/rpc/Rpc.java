package top.fengye.rpc;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import top.fengye.rpc.body.request.AppendEntriesRequest;
import top.fengye.rpc.body.request.RequestVoteRequest;
import top.fengye.rpc.body.response.AppendEntriesResponse;
import top.fengye.rpc.body.response.RequestVoteResponse;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: Rpc调用端 和 RpcHandler处理端
 */
public interface Rpc {

    Future<Void> initHandler();

    Future<AppendEntriesResponse> appendEntries(RpcAddress address, AppendEntriesRequest request);

    Future<RequestVoteResponse> requestVote(RpcAddress address, RequestVoteRequest request);
}

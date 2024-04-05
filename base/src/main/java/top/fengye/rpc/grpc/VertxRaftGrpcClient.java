package top.fengye.rpc.grpc;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.grpc.common.GrpcStatus;

public class VertxRaftGrpcClient {
  private final GrpcClient client;
  private final SocketAddress socketAddress;

  public VertxRaftGrpcClient(GrpcClient client, SocketAddress socketAddress) {
    this.client = client;
    this.socketAddress = socketAddress;
  }

  public Future<top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse> queryElectionStatus(top.fengye.rpc.grpc.Grpc.Empty request) {
    return client.request(socketAddress, RaftGrpc.getQueryElectionStatusMethod()).compose(req -> {
      req.end(request);
      return req.response().compose(resp -> resp.last());
    });
  }

  public Future<top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> applyVote(top.fengye.rpc.grpc.Grpc.ApplyVoteRequest request) {
    return client.request(socketAddress, RaftGrpc.getApplyVoteMethod()).compose(req -> {
      req.end(request);
      return req.response().compose(resp -> resp.last());
    });
  }

  public Future<top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> appendEntries(top.fengye.rpc.grpc.Grpc.AppendEntriesRequest request) {
    return client.request(socketAddress, RaftGrpc.getAppendEntriesMethod()).compose(req -> {
      req.end(request);
      return req.response().compose(resp -> resp.last());
    });
  }

}

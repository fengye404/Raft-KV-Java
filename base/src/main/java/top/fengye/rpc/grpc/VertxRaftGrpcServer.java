package top.fengye.rpc.grpc;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerResponse;

import java.util.ArrayList;
import java.util.List;

public class VertxRaftGrpcServer  {
  public interface RaftApi {
    default Future<top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> appendEntries(top.fengye.rpc.grpc.Grpc.AppendEntriesRequest request) {
      throw new UnsupportedOperationException("Not implemented");
    }
    default void appendEntries(top.fengye.rpc.grpc.Grpc.AppendEntriesRequest request, Promise<top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> response) {
      appendEntries(request)
        .onSuccess(msg -> response.complete(msg))
        .onFailure(error -> response.fail(error));
    }
    default Future<top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> applyVote(top.fengye.rpc.grpc.Grpc.ApplyVoteRequest request) {
      throw new UnsupportedOperationException("Not implemented");
    }
    default void applyVote(top.fengye.rpc.grpc.Grpc.ApplyVoteRequest request, Promise<top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> response) {
      applyVote(request)
        .onSuccess(msg -> response.complete(msg))
        .onFailure(error -> response.fail(error));
    }

    default RaftApi bind_appendEntries(GrpcServer server) {
      server.callHandler(RaftGrpc.getAppendEntriesMethod(), request -> {
        Promise<top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> promise = Promise.promise();
        request.handler(req -> {
          try {
            appendEntries(req, promise);
          } catch (RuntimeException err) {
            promise.tryFail(err);
          }
        });
        promise.future()
          .onFailure(err -> request.response().status(GrpcStatus.INTERNAL).end())
          .onSuccess(resp -> request.response().end(resp));
      });
      return this;
    }
    default RaftApi bind_applyVote(GrpcServer server) {
      server.callHandler(RaftGrpc.getApplyVoteMethod(), request -> {
        Promise<top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> promise = Promise.promise();
        request.handler(req -> {
          try {
            applyVote(req, promise);
          } catch (RuntimeException err) {
            promise.tryFail(err);
          }
        });
        promise.future()
          .onFailure(err -> request.response().status(GrpcStatus.INTERNAL).end())
          .onSuccess(resp -> request.response().end(resp));
      });
      return this;
    }

    default RaftApi bindAll(GrpcServer server) {
      bind_appendEntries(server);
      bind_applyVote(server);
      return this;
    }
  }
}

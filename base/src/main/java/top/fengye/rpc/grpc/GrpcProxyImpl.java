package top.fengye.rpc.grpc;

import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import top.fengye.rpc.RpcProxy;

/**
 * @author: FengYe
 * @date: 2024/4/3 1:18
 * @description: GrpcProxyImpl
 */
public class GrpcProxyImpl implements RpcProxy {
    
    private Vertx vertx;

    @Override
    public void mountVertxInstance(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void startHandler(int port) {

    }

    @Override
    public Promise<Grpc.ApplyVoteResponse> applyVote(Grpc.ApplyVoteRequest request) {
        return null;
    }

    @Override
    public Promise<Grpc.AppendEntriesResponse> appendEntries(Grpc.AppendEntriesRequest request) {
        return null;
    }

}

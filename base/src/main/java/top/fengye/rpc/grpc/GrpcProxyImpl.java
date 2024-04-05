package top.fengye.rpc.grpc;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.server.GrpcServer;
import lombok.extern.slf4j.Slf4j;
import top.fengye.raft.RaftNode;
import top.fengye.raft.RoleEnum;
import top.fengye.rpc.RpcAddress;
import top.fengye.rpc.RpcProxy;
import top.fengye.rpc.config.RpcConstants;

import javax.imageio.stream.MemoryCacheImageOutputStream;

/**
 * @author: FengYe
 * @date: 2024/4/3 1:18
 * @description: GrpcProxy 的 Grpc 实现。每个 RaftNode 都会持有一个 GrpcProxyImpl 实例
 */
@Slf4j
public class GrpcProxyImpl implements RpcProxy {

    private RaftNode raftNode;

    private Vertx vertx;

    private SocketAddress socketAddress;

    private VertxRaftGrpcClient client;

    public GrpcProxyImpl(RaftNode raftNode) {
        mountHandler(raftNode);
    }

    @Override
    public void mountHandler(RaftNode node) {
        this.raftNode = node;
        this.vertx = node.getVertx();
        GrpcServer grpcServer = GrpcServer.server(vertx);
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(grpcServer)
                .listen(raftNode.getRpcAddress().getPort());

        VertxRaftGrpcServer.RaftApi stub = new VertxRaftGrpcServer.RaftApi() {
            @Override
            public void applyVote(Grpc.ApplyVoteRequest request, Promise<Grpc.ApplyVoteResponse> response) {
                raftNode.setLastHeartBeat(System.currentTimeMillis());
                String requestNodeId = request.getNodeId();
                long requestTerm = request.getTerm();
                boolean agreed = false;
                // 如果 candidate 的 term 小于 follower 的 term，那么 follower 不会给该 candidate 投票
                // 如果 candidate 的 term 等于 follower 的 term，那么 follower 会先比较 candidate 的日志和自己的日志，
                //   如果 candidate 的日志不落后于 follower 且 follower 在这个任期没有给其他的 candidate 投票，那么 follower 会给该 candidate 投票
                // 如果 candidate 的 term 大于 follower 的 term，那么 follower 会认为自己的选期落后了，会直接投票给该 candidate，并更新自己的 term
                if (requestTerm > raftNode.getCurrentTerm()) {
                    agreed = true;
                    raftNode.setVotedFor(requestNodeId);
                    raftNode.setCurrentTerm(requestTerm);
                } else if (requestTerm == raftNode.getCurrentTerm() && raftNode.getVotedFor() == null) {
                    // TODO 添加 log 相关选举约束逻辑
                    agreed = true;
                    raftNode.setVotedFor(requestNodeId);
                }
                response.complete(
                        Grpc.ApplyVoteResponse.newBuilder()
                                .setAgreed(agreed)
                                .setTerm(raftNode.getCurrentTerm())
                                .setNodeId(raftNode.getNodeId())
                                .build()
                );
            }

            @Override
            public void appendEntries(Grpc.AppendEntriesRequest request, Promise<Grpc.AppendEntriesResponse> response) {
                // 如果请求过来的 term 小于自己的，则拒绝接收
                if (request.getTerm() < raftNode.getCurrentTerm()) {
                    response.complete(
                            Grpc.AppendEntriesResponse.newBuilder()
                                    .setNodeId(raftNode.getNodeId())
                                    .setTerm(raftNode.getCurrentTerm())
                                    .setSuccess(false)
                                    .build()
                    );
                } else {
                    raftNode.becomeFollow(request.getTerm());
                    response.complete(
                            Grpc.AppendEntriesResponse.newBuilder()
                                    .setNodeId(raftNode.getNodeId())
                                    .setTerm(raftNode.getCurrentTerm())
                                    .setSuccess(true)
                                    .build()
                    );
                }
            }

            @Override
            public void queryElectionStatus(Grpc.Empty empty, Promise<Grpc.queryElectionStatusResponse> response) {
                response.complete(
                        Grpc.queryElectionStatusResponse.newBuilder()
                                .setNodeId(raftNode.getNodeId())
                                .setRole(raftNode.getRole().name() + ":" + raftNode.getCurrentTerm())
                                .build()
                );
            }

        };
        stub.bindAll(grpcServer);
    }

    @Override
    public Future<Grpc.ApplyVoteResponse> applyVote(RpcAddress rpcAddress, Grpc.ApplyVoteRequest request) {
        refreshGrpcClient(rpcAddress);
        return client.applyVote(request);
    }

    @Override
    public Future<Grpc.AppendEntriesResponse> appendEntries(RpcAddress rpcAddress, Grpc.AppendEntriesRequest request) {
        refreshGrpcClient(rpcAddress);
        return client.appendEntries(request);
    }

    /**
     * gprcClient 将被缓存，除非需要发送的 address 与上一次不一致，此时需要刷新
     *
     * @param rpcAddress
     */
    private void refreshGrpcClient(RpcAddress rpcAddress) {
        if (null == client || rpcAddress.getPort() != (socketAddress.port())) {
            GrpcClient grpcClient = GrpcClient.client(vertx);
            socketAddress = SocketAddress.inetSocketAddress(rpcAddress.getPort(), rpcAddress.getHost());
            client = new VertxRaftGrpcClient(grpcClient, socketAddress);
        }
    }
}

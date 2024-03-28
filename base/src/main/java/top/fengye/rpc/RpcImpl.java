package top.fengye.rpc;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.ext.web.handler.BodyHandler;
import top.fengye.raft.RaftNode;
import top.fengye.rpc.body.request.AppendEntriesRequest;
import top.fengye.rpc.body.request.RequestVoteRequest;
import top.fengye.rpc.body.response.AppendEntriesResponse;
import top.fengye.rpc.body.response.RequestVoteResponse;
import top.fengye.rpc.config.RpcConstants;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: RpcImpl
 */
public class RpcImpl implements Rpc {

    private Vertx vertx;

    private WebClient webClient;

    private RaftNode raftNode;

    public RpcImpl(RaftNode raftNode) {
        this.raftNode = raftNode;
        this.webClient = WebClient.create(raftNode.getVertx());
    }

    @Override
    public Future<AppendEntriesResponse> appendEntries(RpcAddress address, AppendEntriesRequest request) {


        return null;
    }

    @Override
    public Future<RequestVoteResponse> requestVote(RpcAddress address, RequestVoteRequest request) {
        return webClient.post(address.getPort(), address.getHost(), RpcConstants.requestVotePath)
                .putHeader(RpcConstants.headerKeyNodeId, raftNode.getNodeId())
                .as(BodyCodec.buffer())
                .sendBuffer(request.toBuffer())
                .map(r -> new RequestVoteResponse(r.body()));
    }

    @Override
    public Future<Void> initHandler() {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        httpServer.requestHandler(router).listen(RpcConstants.raftPort);

        BodyHandler bodyHandler = BodyHandler.create(false);
        router.route(RpcConstants.requestVotePath)
                .handler(bodyHandler)
                .handler(routingContext -> {
                    raftNode.setLastHeatBeat(System.currentTimeMillis());
                    RequestVoteRequest requestBody = new RequestVoteRequest(routingContext.body().buffer());
                    String requestNodeId = routingContext.request().getHeader(RpcConstants.headerKeyNodeId);
                    long requestTerm = Long.parseLong(routingContext.request().getHeader(RpcConstants.headerKeyTerm));

                    // TODO 添加 log 相关选举约束逻辑
                    boolean agreed = false;
                    if (requestTerm > raftNode.getCurrentTerm()) {
                        agreed = true;
                        raftNode.setVotedFor(requestNodeId);
                        raftNode.setCurrentTerm(requestTerm);
                    } else if (requestTerm == raftNode.getCurrentTerm() && raftNode.getVotedFor() == null) {
                        agreed = true;
                        raftNode.setVotedFor(requestNodeId);
                    }
                    routingContext.end(new RequestVoteResponse(agreed, raftNode.getCurrentTerm()).toBuffer());
                });

        router.route(RpcConstants.appendEntries)
                .handler(bodyHandler)
                .handler(routingContext -> {
                    raftNode.setLastHeatBeat(System.currentTimeMillis());

                });

        return null;
    }
}

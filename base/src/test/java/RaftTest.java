import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import top.fengye.raft.RaftNode;
import top.fengye.rpc.RpcAddress;
import top.fengye.rpc.grpc.VertxRaftGrpcClient;
import top.fengye.util.RpcUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: FengYe
 * @date: 2024/3/29 3:09
 * @description: Test
 */
@Slf4j
public class RaftTest {

    private RaftNode raftNode1;
    private RaftNode raftNode2;
    private RaftNode raftNode3;
    private Map<String, RaftNode> peers;

    private VertxOptions vertxOptions;

    private RpcUtils rpcUtils;

    {
        raftNode1 = new RaftNode(new RpcAddress("localhost", 8080));
        raftNode2 = new RaftNode(new RpcAddress("localhost", 8081));
        raftNode3 = new RaftNode(new RpcAddress("localhost", 8082));
        raftNode1.setNodeId("node1");
        raftNode2.setNodeId("node2");
        raftNode3.setNodeId("node3");
        peers = new HashMap<>();
        peers.put(raftNode1.getNodeId(), raftNode1);
        peers.put(raftNode2.getNodeId(), raftNode2);
        peers.put(raftNode3.getNodeId(), raftNode3);
        raftNode1.loadPeers(peers);
        raftNode2.loadPeers(peers);
        raftNode3.loadPeers(peers);
        vertxOptions = new VertxOptions()
                .setBlockedThreadCheckInterval(10000000L)
                .setBlockedThreadCheckIntervalUnit(TimeUnit.DAYS)
                .setEventLoopPoolSize(1)
                .setWorkerPoolSize(1)
                .setInternalBlockingPoolSize(1);
        rpcUtils = new RpcUtils();
    }

    @Test
    public void queryElectionStatus() {
        Vertx vertx = Vertx.vertx();
        TestClientVerticle testClientVerticle = new TestClientVerticle();
        vertx.deployVerticle(testClientVerticle);
        while (true) ;
    }

    @Test
    public void deploy1() {
        Vertx vertx1 = Vertx.vertx(vertxOptions);
        vertx1.deployVerticle(raftNode1);
        while (true) ;
    }

    @Test
    public void deploy2() {
        Vertx vertx2 = Vertx.vertx(vertxOptions);
        vertx2.deployVerticle(raftNode2);
        while (true) ;
    }

    @Test
    public void deploy3() {
        Vertx vertx3 = Vertx.vertx(vertxOptions);
        vertx3.deployVerticle(raftNode3);
        while (true) ;
    }


    private GrpcClient grpcClient;

    private SocketAddress socketAddress;

    private VertxRaftGrpcClient vertxRaftGrpcClient;

    @Test
    public void testCommand() {
        rpcUtils.put(raftNode2.getRpcAddress(), "hello", "raft")
                .onSuccess(res -> {
                    log.info("=== {} ===", res);
                });
        while (true) ;
    }
}

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import top.fengye.biz.Command;
import top.fengye.raft.RaftNode;
import top.fengye.rpc.RpcAddress;
import top.fengye.rpc.grpc.BizParam;
import top.fengye.rpc.grpc.Grpc;
import top.fengye.rpc.grpc.VertxRaftGrpcClient;

import java.util.Collection;
import java.util.Collections;
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

    private String deployId1;
    private String deployId2;
    private String deployId3;

    public static void main(String[] args) throws InvalidProtocolBufferException, InterruptedException {

        Vertx vertx = Vertx.vertx();

        TestServerVerticle testServerVerticle = new TestServerVerticle();
        vertx.deployVerticle(testServerVerticle);

        Thread.sleep(3000L);


        TestClientVerticle testClientVerticle = new TestClientVerticle();
        vertx.deployVerticle(testClientVerticle);

        while (true) ;
    }

    @Test
    public void queryElectionStatus() {
        Vertx vertx = Vertx.vertx();
        TestClientVerticle testClientVerticle = new TestClientVerticle();
        vertx.deployVerticle(testClientVerticle);
        while (true) ;
    }

    @Test
    public void testElection() {
        raftNode1 = new RaftNode(new RpcAddress("localhost", 8080));
        raftNode2 = new RaftNode(new RpcAddress("localhost", 8081));
        raftNode3 = new RaftNode(new RpcAddress("localhost", 8082));

        raftNode1.setNodeId("node1");
        raftNode2.setNodeId("node2");
        raftNode3.setNodeId("node3");

        Map<String, RaftNode> peers = new HashMap<>();
        peers.put(raftNode1.getNodeId(), raftNode1);
        peers.put(raftNode2.getNodeId(), raftNode2);
        peers.put(raftNode3.getNodeId(), raftNode3);
        raftNode1.loadPeers(peers);
        raftNode2.loadPeers(peers);
        raftNode3.loadPeers(peers);

        VertxOptions vertxOptions = new VertxOptions()
                .setBlockedThreadCheckInterval(10000000L)
                .setBlockedThreadCheckIntervalUnit(TimeUnit.DAYS)
                .setEventLoopPoolSize(1)
                .setWorkerPoolSize(1)
                .setInternalBlockingPoolSize(1);
        Vertx vertx1 = Vertx.vertx(vertxOptions);
        Vertx vertx2 = Vertx.vertx(vertxOptions);
        Vertx vertx3 = Vertx.vertx(vertxOptions);


        vertx1.deployVerticle(raftNode1).onSuccess(s -> deployId1 = s);
        vertx2.deployVerticle(raftNode2).onSuccess(s -> deployId2 = s);
        vertx3.deployVerticle(raftNode3).onSuccess(s -> deployId3 = s);

        while (true) ;
    }


    @Test
    public void test(){
        System.out.println(Command.CommandType.parse(BizParam.CommandType.GET));
    }
}

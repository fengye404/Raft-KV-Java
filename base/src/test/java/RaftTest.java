import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.core.Vertx;
import org.junit.Test;
import top.fengye.raft.RaftNode;
import top.fengye.rpc.RpcAddress;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: FengYe
 * @date: 2024/3/29 3:09
 * @description: Test
 */
public class RaftTest {
    public static void main(String[] args) throws InvalidProtocolBufferException, InterruptedException {

        Vertx vertx = Vertx.vertx();

        TestServerVerticle testServerVerticle = new TestServerVerticle();
        vertx.deployVerticle(testServerVerticle);

        Thread.sleep(3000L);


        TestClientVerticle testClientVerticle = new TestClientVerticle();
        vertx.deployVerticle(testClientVerticle);

        while (true);
    }

    @Test
    public void queryElectionStatus(){
        Vertx vertx = Vertx.vertx();
        TestClientVerticle testClientVerticle = new TestClientVerticle();
        vertx.deployVerticle(testClientVerticle);
        while (true);
    }


    @Test
    public void testElection(){
        RaftNode raftNode1 = new RaftNode(new RpcAddress("localhost",8080));
        RaftNode raftNode2 = new RaftNode(new RpcAddress("localhost",8081));
        RaftNode raftNode3 = new RaftNode(new RpcAddress("localhost",8082));

        raftNode1.setNodeId("node1");
        raftNode2.setNodeId("node2");
        raftNode3.setNodeId("node3");

        Map<String,RaftNode> peers = new HashMap<>();
        peers.put(raftNode1.getNodeId(),raftNode1);
        peers.put(raftNode2.getNodeId(),raftNode2);
        peers.put(raftNode3.getNodeId(),raftNode3);
        raftNode1.setPeers(peers);
        raftNode2.setPeers(peers);
        raftNode3.setPeers(peers);

        Vertx vertx1 = Vertx.vertx();
        Vertx vertx2 = Vertx.vertx();
        Vertx vertx3 = Vertx.vertx();

        vertx1.deployVerticle(raftNode1);
        vertx2.deployVerticle(raftNode2);
        vertx3.deployVerticle(raftNode3);

        while (true);
    }
}

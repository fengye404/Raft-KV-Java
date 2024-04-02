import com.google.protobuf.InvalidProtocolBufferException;
import io.vertx.core.Vertx;
import top.fengye.rpc.grpc.Grpc;
import top.fengye.rpc.grpc.VertxRaftGrpcClient;
import top.fengye.rpc.grpc.VertxRaftGrpcServer;

/**
 * @author: FengYe
 * @date: 2024/3/29 3:09
 * @description: Test
 */
public class Test {
    public static void main(String[] args) throws InvalidProtocolBufferException, InterruptedException {

        Vertx vertx = Vertx.vertx();

        TestServerVerticle testServerVerticle = new TestServerVerticle();
        vertx.deployVerticle(testServerVerticle);

        Thread.sleep(3000L);


        TestClientVerticle testClientVerticle = new TestClientVerticle();
        vertx.deployVerticle(testClientVerticle);

        while (true);
    }
}

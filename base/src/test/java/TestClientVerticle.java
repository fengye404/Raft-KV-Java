import io.grpc.MethodDescriptor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientRequest;
import top.fengye.rpc.grpc.Grpc;
import top.fengye.rpc.grpc.RaftGrpc;
import top.fengye.rpc.grpc.VertxRaftGrpcClient;

/**
 * @author: FengYe
 * @date: 2024/4/1 23:34
 * @description: TestClientVerticle
 */
public class TestClientVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        GrpcClient grpcClient = GrpcClient.client(vertx);
        SocketAddress server = SocketAddress.inetSocketAddress(1234, "localhost");
        VertxRaftGrpcClient client = new VertxRaftGrpcClient(grpcClient, server);
        client.appendEntries(Grpc.AppendEntriesRequest.newBuilder().setId("request").build())
                .onSuccess(response->{
                    System.out.println(response.getId());
                });
    }
}

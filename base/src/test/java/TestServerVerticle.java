import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerResponse;
import top.fengye.rpc.grpc.Grpc;
import top.fengye.rpc.grpc.RaftGrpc;
import top.fengye.rpc.grpc.VertxRaftGrpcServer;

/**
 * @author: FengYe
 * @date: 2024/4/1 23:25
 * @description: TestVerticle
 */
public class TestServerVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        GrpcServer grpcServer = GrpcServer.server(vertx);
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(grpcServer)
                .listen(1234);


        VertxRaftGrpcServer.RaftApi stub = new VertxRaftGrpcServer.RaftApi() {
            @Override
            public void appendEntries(Grpc.AppendEntriesRequest request, Promise<Grpc.AppendEntriesResponse> response) {
                System.out.println(request.getNodeId());
                response.complete(Grpc.AppendEntriesResponse.newBuilder().setNodeId("response").build());
            }
        };

        stub.bindAll(grpcServer);


    }
}

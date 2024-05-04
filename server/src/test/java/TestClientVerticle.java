import io.grpc.MethodDescriptor;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import io.vertx.grpc.client.GrpcClientRequest;
import lombok.extern.slf4j.Slf4j;
import top.fengye.rpc.grpc.Grpc;
import top.fengye.rpc.grpc.RaftGrpc;
import top.fengye.rpc.grpc.VertxRaftGrpcClient;

import java.awt.*;

/**
 * @author: FengYe
 * @date: 2024/4/1 23:34
 * @description: TestClientVerticle
 */
@Slf4j
public class TestClientVerticle extends AbstractVerticle {

    private GrpcClient grpcClient;

    private SocketAddress socketAddress;

    private VertxRaftGrpcClient vertxRaftGrpcClient;

    @Override
    public void start() {
        try {
            vertx.setPeriodic(2000, id -> {
                log.info("=================================");
                grpcClient = GrpcClient.client(vertx);
                socketAddress = SocketAddress.inetSocketAddress(8080, "localhost");
                vertxRaftGrpcClient = new VertxRaftGrpcClient(grpcClient, socketAddress);
                vertxRaftGrpcClient.queryStatus(Grpc.Empty.newBuilder().build())
                        .onSuccess(res -> {
                            log.info("{},{}", res.getNodeId(), res.getRoleInfo());
                            log.info("{}", res.getEntriesInfo());
                        });
                grpcClient = GrpcClient.client(vertx);
                socketAddress = SocketAddress.inetSocketAddress(8081, "localhost");
                vertxRaftGrpcClient = new VertxRaftGrpcClient(grpcClient, socketAddress);
                vertxRaftGrpcClient.queryStatus(Grpc.Empty.newBuilder().build())
                        .onSuccess(res -> {
                            log.info("{},{}", res.getNodeId(), res.getRoleInfo());
                            log.info("{}", res.getEntriesInfo());
                        });
                grpcClient = GrpcClient.client(vertx);
                socketAddress = SocketAddress.inetSocketAddress(8082, "localhost");
                vertxRaftGrpcClient = new VertxRaftGrpcClient(grpcClient, socketAddress);
                vertxRaftGrpcClient.queryStatus(Grpc.Empty.newBuilder().build())
                        .onSuccess(res -> {
                            log.info("{},{}", res.getNodeId(), res.getRoleInfo());
                            log.info("{}", res.getEntriesInfo());
                        });
                log.info("=================================");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

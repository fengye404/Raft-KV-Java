package top.fengye.util;

import com.google.protobuf.ByteString;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.net.SocketAddress;
import io.vertx.grpc.client.GrpcClient;
import top.fengye.rpc.RpcAddress;
import top.fengye.rpc.grpc.BizParam;
import top.fengye.rpc.grpc.Grpc;
import top.fengye.rpc.grpc.VertxRaftGrpcClient;

/**
 * @author: FengYe
 * @date: 2024/4/23 上午12:27
 * @description: Utils
 */
public class RpcUtils {

    private SocketAddress socketAddress;

    private VertxRaftGrpcClient vertxRaftGrpcClient;

    private Vertx vertx;

    private GrpcClient grpcClient;

    {
        vertx = Vertx.vertx();
        grpcClient = GrpcClient.client(vertx);
    }

    public Future<Grpc.CommandResponse> put(RpcAddress address, String key, String value) {
        socketAddress = SocketAddress.inetSocketAddress(address.getPort(), address.getHost());
        vertxRaftGrpcClient = new VertxRaftGrpcClient(grpcClient, socketAddress);
        return vertxRaftGrpcClient.handleRequest(
                Grpc.CommandRequest.newBuilder()
                        .setCommand(
                                BizParam.Command.newBuilder()
                                        .setType(BizParam.CommandType.PUT)
                                        .setKey(BizParam.Key.newBuilder().setData(ByteString.copyFromUtf8(key)))
                                        .setValue(BizParam.Value.newBuilder().setData(ByteString.copyFromUtf8(value)))
                                        .build()
                        )
                        .build()
        );
    }

    public Future<Grpc.CommandResponse> get(RpcAddress address, String key, String value) {
        socketAddress = SocketAddress.inetSocketAddress(address.getPort(), address.getHost());
        vertxRaftGrpcClient = new VertxRaftGrpcClient(grpcClient, socketAddress);
        return vertxRaftGrpcClient.handleRequest(
                Grpc.CommandRequest.newBuilder()
                        .setCommand(
                                BizParam.Command.newBuilder()
                                        .setType(BizParam.CommandType.GET)
                                        .setKey(BizParam.Key.newBuilder().setData(ByteString.copyFromUtf8(key)))
                                        .setValue(BizParam.Value.newBuilder().setData(ByteString.copyFromUtf8(value)))
                                        .build()
                        )
                        .build()
        );
    }
}

import com.google.protobuf.InvalidProtocolBufferException;
import top.fengye.rpc.grpc.GrpcStub;

/**
 * @author: FengYe
 * @date: 2024/3/29 3:09
 * @description: Test
 */
public class Test {
    public static void main(String[] args) throws InvalidProtocolBufferException {
        GrpcStub.HelloRequest test = GrpcStub.HelloRequest.newBuilder().setName("test").build();

        byte[] byteArray = test.toByteArray();
        GrpcStub.HelloRequest helloRequest = GrpcStub.HelloRequest.parseFrom(byteArray);

        System.out.println(helloRequest.toString());
    }
}

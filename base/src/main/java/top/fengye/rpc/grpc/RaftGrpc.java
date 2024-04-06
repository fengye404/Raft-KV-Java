package top.fengye.rpc.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.50.2)",
    comments = "Source: grpc.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class RaftGrpc {

  private RaftGrpc() {}

  public static final String SERVICE_NAME = "top.fengye.rpc.grpc.Raft";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.Empty,
      top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse> getQueryElectionStatusMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "queryElectionStatus",
      requestType = top.fengye.rpc.grpc.Grpc.Empty.class,
      responseType = top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.Empty,
      top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse> getQueryElectionStatusMethod() {
    io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.Empty, top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse> getQueryElectionStatusMethod;
    if ((getQueryElectionStatusMethod = RaftGrpc.getQueryElectionStatusMethod) == null) {
      synchronized (RaftGrpc.class) {
        if ((getQueryElectionStatusMethod = RaftGrpc.getQueryElectionStatusMethod) == null) {
          RaftGrpc.getQueryElectionStatusMethod = getQueryElectionStatusMethod =
              io.grpc.MethodDescriptor.<top.fengye.rpc.grpc.Grpc.Empty, top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "queryElectionStatus"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  top.fengye.rpc.grpc.Grpc.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse.getDefaultInstance()))
              .setSchemaDescriptor(new RaftMethodDescriptorSupplier("queryElectionStatus"))
              .build();
        }
      }
    }
    return getQueryElectionStatusMethod;
  }

  private static volatile io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.Empty,
      top.fengye.rpc.grpc.Grpc.shutDownResponse> getShutDownMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "shutDown",
      requestType = top.fengye.rpc.grpc.Grpc.Empty.class,
      responseType = top.fengye.rpc.grpc.Grpc.shutDownResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.Empty,
      top.fengye.rpc.grpc.Grpc.shutDownResponse> getShutDownMethod() {
    io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.Empty, top.fengye.rpc.grpc.Grpc.shutDownResponse> getShutDownMethod;
    if ((getShutDownMethod = RaftGrpc.getShutDownMethod) == null) {
      synchronized (RaftGrpc.class) {
        if ((getShutDownMethod = RaftGrpc.getShutDownMethod) == null) {
          RaftGrpc.getShutDownMethod = getShutDownMethod =
              io.grpc.MethodDescriptor.<top.fengye.rpc.grpc.Grpc.Empty, top.fengye.rpc.grpc.Grpc.shutDownResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "shutDown"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  top.fengye.rpc.grpc.Grpc.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  top.fengye.rpc.grpc.Grpc.shutDownResponse.getDefaultInstance()))
              .setSchemaDescriptor(new RaftMethodDescriptorSupplier("shutDown"))
              .build();
        }
      }
    }
    return getShutDownMethod;
  }

  private static volatile io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.ApplyVoteRequest,
      top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> getApplyVoteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "applyVote",
      requestType = top.fengye.rpc.grpc.Grpc.ApplyVoteRequest.class,
      responseType = top.fengye.rpc.grpc.Grpc.ApplyVoteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.ApplyVoteRequest,
      top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> getApplyVoteMethod() {
    io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.ApplyVoteRequest, top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> getApplyVoteMethod;
    if ((getApplyVoteMethod = RaftGrpc.getApplyVoteMethod) == null) {
      synchronized (RaftGrpc.class) {
        if ((getApplyVoteMethod = RaftGrpc.getApplyVoteMethod) == null) {
          RaftGrpc.getApplyVoteMethod = getApplyVoteMethod =
              io.grpc.MethodDescriptor.<top.fengye.rpc.grpc.Grpc.ApplyVoteRequest, top.fengye.rpc.grpc.Grpc.ApplyVoteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "applyVote"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  top.fengye.rpc.grpc.Grpc.ApplyVoteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  top.fengye.rpc.grpc.Grpc.ApplyVoteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new RaftMethodDescriptorSupplier("applyVote"))
              .build();
        }
      }
    }
    return getApplyVoteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.AppendEntriesRequest,
      top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> getAppendEntriesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "appendEntries",
      requestType = top.fengye.rpc.grpc.Grpc.AppendEntriesRequest.class,
      responseType = top.fengye.rpc.grpc.Grpc.AppendEntriesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.AppendEntriesRequest,
      top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> getAppendEntriesMethod() {
    io.grpc.MethodDescriptor<top.fengye.rpc.grpc.Grpc.AppendEntriesRequest, top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> getAppendEntriesMethod;
    if ((getAppendEntriesMethod = RaftGrpc.getAppendEntriesMethod) == null) {
      synchronized (RaftGrpc.class) {
        if ((getAppendEntriesMethod = RaftGrpc.getAppendEntriesMethod) == null) {
          RaftGrpc.getAppendEntriesMethod = getAppendEntriesMethod =
              io.grpc.MethodDescriptor.<top.fengye.rpc.grpc.Grpc.AppendEntriesRequest, top.fengye.rpc.grpc.Grpc.AppendEntriesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "appendEntries"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  top.fengye.rpc.grpc.Grpc.AppendEntriesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  top.fengye.rpc.grpc.Grpc.AppendEntriesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new RaftMethodDescriptorSupplier("appendEntries"))
              .build();
        }
      }
    }
    return getAppendEntriesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RaftStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RaftStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RaftStub>() {
        @java.lang.Override
        public RaftStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RaftStub(channel, callOptions);
        }
      };
    return RaftStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RaftBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RaftBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RaftBlockingStub>() {
        @java.lang.Override
        public RaftBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RaftBlockingStub(channel, callOptions);
        }
      };
    return RaftBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RaftFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RaftFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RaftFutureStub>() {
        @java.lang.Override
        public RaftFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RaftFutureStub(channel, callOptions);
        }
      };
    return RaftFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class RaftImplBase implements io.grpc.BindableService {

    /**
     */
    public void queryElectionStatus(top.fengye.rpc.grpc.Grpc.Empty request,
        io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getQueryElectionStatusMethod(), responseObserver);
    }

    /**
     */
    public void shutDown(top.fengye.rpc.grpc.Grpc.Empty request,
        io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.shutDownResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getShutDownMethod(), responseObserver);
    }

    /**
     */
    public void applyVote(top.fengye.rpc.grpc.Grpc.ApplyVoteRequest request,
        io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getApplyVoteMethod(), responseObserver);
    }

    /**
     */
    public void appendEntries(top.fengye.rpc.grpc.Grpc.AppendEntriesRequest request,
        io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAppendEntriesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getQueryElectionStatusMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                top.fengye.rpc.grpc.Grpc.Empty,
                top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse>(
                  this, METHODID_QUERY_ELECTION_STATUS)))
          .addMethod(
            getShutDownMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                top.fengye.rpc.grpc.Grpc.Empty,
                top.fengye.rpc.grpc.Grpc.shutDownResponse>(
                  this, METHODID_SHUT_DOWN)))
          .addMethod(
            getApplyVoteMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                top.fengye.rpc.grpc.Grpc.ApplyVoteRequest,
                top.fengye.rpc.grpc.Grpc.ApplyVoteResponse>(
                  this, METHODID_APPLY_VOTE)))
          .addMethod(
            getAppendEntriesMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                top.fengye.rpc.grpc.Grpc.AppendEntriesRequest,
                top.fengye.rpc.grpc.Grpc.AppendEntriesResponse>(
                  this, METHODID_APPEND_ENTRIES)))
          .build();
    }
  }

  /**
   */
  public static final class RaftStub extends io.grpc.stub.AbstractAsyncStub<RaftStub> {
    private RaftStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RaftStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RaftStub(channel, callOptions);
    }

    /**
     */
    public void queryElectionStatus(top.fengye.rpc.grpc.Grpc.Empty request,
        io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getQueryElectionStatusMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void shutDown(top.fengye.rpc.grpc.Grpc.Empty request,
        io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.shutDownResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getShutDownMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void applyVote(top.fengye.rpc.grpc.Grpc.ApplyVoteRequest request,
        io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getApplyVoteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void appendEntries(top.fengye.rpc.grpc.Grpc.AppendEntriesRequest request,
        io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAppendEntriesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class RaftBlockingStub extends io.grpc.stub.AbstractBlockingStub<RaftBlockingStub> {
    private RaftBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RaftBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RaftBlockingStub(channel, callOptions);
    }

    /**
     */
    public top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse queryElectionStatus(top.fengye.rpc.grpc.Grpc.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getQueryElectionStatusMethod(), getCallOptions(), request);
    }

    /**
     */
    public top.fengye.rpc.grpc.Grpc.shutDownResponse shutDown(top.fengye.rpc.grpc.Grpc.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getShutDownMethod(), getCallOptions(), request);
    }

    /**
     */
    public top.fengye.rpc.grpc.Grpc.ApplyVoteResponse applyVote(top.fengye.rpc.grpc.Grpc.ApplyVoteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getApplyVoteMethod(), getCallOptions(), request);
    }

    /**
     */
    public top.fengye.rpc.grpc.Grpc.AppendEntriesResponse appendEntries(top.fengye.rpc.grpc.Grpc.AppendEntriesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAppendEntriesMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class RaftFutureStub extends io.grpc.stub.AbstractFutureStub<RaftFutureStub> {
    private RaftFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected RaftFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RaftFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse> queryElectionStatus(
        top.fengye.rpc.grpc.Grpc.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getQueryElectionStatusMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<top.fengye.rpc.grpc.Grpc.shutDownResponse> shutDown(
        top.fengye.rpc.grpc.Grpc.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getShutDownMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<top.fengye.rpc.grpc.Grpc.ApplyVoteResponse> applyVote(
        top.fengye.rpc.grpc.Grpc.ApplyVoteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getApplyVoteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<top.fengye.rpc.grpc.Grpc.AppendEntriesResponse> appendEntries(
        top.fengye.rpc.grpc.Grpc.AppendEntriesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAppendEntriesMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_QUERY_ELECTION_STATUS = 0;
  private static final int METHODID_SHUT_DOWN = 1;
  private static final int METHODID_APPLY_VOTE = 2;
  private static final int METHODID_APPEND_ENTRIES = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RaftImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(RaftImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_QUERY_ELECTION_STATUS:
          serviceImpl.queryElectionStatus((top.fengye.rpc.grpc.Grpc.Empty) request,
              (io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.queryElectionStatusResponse>) responseObserver);
          break;
        case METHODID_SHUT_DOWN:
          serviceImpl.shutDown((top.fengye.rpc.grpc.Grpc.Empty) request,
              (io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.shutDownResponse>) responseObserver);
          break;
        case METHODID_APPLY_VOTE:
          serviceImpl.applyVote((top.fengye.rpc.grpc.Grpc.ApplyVoteRequest) request,
              (io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.ApplyVoteResponse>) responseObserver);
          break;
        case METHODID_APPEND_ENTRIES:
          serviceImpl.appendEntries((top.fengye.rpc.grpc.Grpc.AppendEntriesRequest) request,
              (io.grpc.stub.StreamObserver<top.fengye.rpc.grpc.Grpc.AppendEntriesResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class RaftBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RaftBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return top.fengye.rpc.grpc.Grpc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Raft");
    }
  }

  private static final class RaftFileDescriptorSupplier
      extends RaftBaseDescriptorSupplier {
    RaftFileDescriptorSupplier() {}
  }

  private static final class RaftMethodDescriptorSupplier
      extends RaftBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    RaftMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RaftGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RaftFileDescriptorSupplier())
              .addMethod(getQueryElectionStatusMethod())
              .addMethod(getShutDownMethod())
              .addMethod(getApplyVoteMethod())
              .addMethod(getAppendEntriesMethod())
              .build();
        }
      }
    }
    return result;
  }
}

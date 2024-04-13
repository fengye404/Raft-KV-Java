package top.fengye.raft;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import top.fengye.biz.Command;
import top.fengye.rpc.RpcAddress;
import top.fengye.rpc.RpcProxy;
import top.fengye.rpc.grpc.Grpc;
import top.fengye.rpc.grpc.GrpcProxyImpl;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: RaftNode
 */
@Getter
@Setter
@Slf4j
public class RaftNode extends AbstractVerticle implements Serializable {
    private static final long serialVersionUID = -1;
    private String nodeId;
    private String leaderId;
    private RaftLog raftLog;
    private RaftStateMachine raftStateMachine;
    private long currentTerm;
    private String votedFor;
    private RoleEnum role;
    private Map<String, RaftNode> peers;
    private Map<String, Long> nextLogIndexMap;
    private RpcAddress rpcAddress;
    private RpcProxy rpcProxy;
    private Long heartBeatPeriodicId;
    /**
     * 最后一次心跳时间，用于选举计时器计算
     */
    private long lastHeartBeat;
    /**
     * 选举超时时长，每次选举后都会重置，范围为 150ms ~ 300ms
     */
    private long electionTimeout;
    /**
     * 心跳间隔
     */
    private static final int HEARTBEAT_INTERVAL = 1000;
    /**
     * 最小选举超时
     */
    private static final int MIN_ELECTION_TIMEOUT = 1500;
    /**
     * 最大选举超时
     */
    private static final int MAX_ELECTION_TIMEOUT = 3000;

    private final Random random = new Random();

    public RaftNode(RpcAddress rpcAddress) {
        this.nodeId = UUID.randomUUID().toString();
        this.raftLog = new RaftLog();
        this.lastHeartBeat = System.currentTimeMillis();
        this.currentTerm = 0L;
        this.votedFor = null;
        this.role = RoleEnum.Follower;
        this.peers = new HashMap<>();
        this.nextLogIndexMap = new HashMap<>();
        this.rpcAddress = rpcAddress;
        resetElectionTimeout();
    }

    @Override
    public void start() throws Exception {
        this.rpcProxy = new GrpcProxyImpl(this);

        startElectionTimer();
        super.start();
    }

    /**
     * 选举计时器超时后，将发起选举，并重置自己的超时时间
     */
    public void startElectionTimer() {
        vertx.setPeriodic(electionTimeout, id -> {
            if (RoleEnum.Leader != role &&
                    System.currentTimeMillis() - lastHeartBeat > electionTimeout) {
                startElection();
                resetElectionTimeout();
            }
        });
    }

    public void startElection() {
        becomeCandidate();

        // peers 中不包含自己，过半数量直接取 peers.size() / 2，因为自己默认已经给自己投票了
        AtomicInteger majority = new AtomicInteger(peers.size() / 2);
        Promise<Void> promise = Promise.promise();
        AtomicBoolean completeStatus = new AtomicBoolean(false);

        // 给所有其他节点发送投票信息
        for (RaftNode node : peers.values()) {
            rpcProxy.applyVote(node.getRpcAddress(),
                    Grpc.ApplyVoteRequest
                            .newBuilder()
                            .setNodeId(nodeId)
                            .setTerm(currentTerm)
                            .build()
            ).onSuccess(res -> {
                // 如果自己的 term 小于一个 peer 的 term，说明选期已经落后，直接变为 follower，以减少不必要的选举
                if (res.getTerm() > currentTerm) {
                    becomeFollow(res.getTerm(), null);
                }
                if (res.getAgreed()) {
                    // 前一个判断条件用于 cas 判断得到的票数是否过半
                    // 后一个判断条件用于防止 promise 被重复 complete
                    if (majority.decrementAndGet() == 0 && completeStatus.compareAndSet(false, true)) {
                        promise.complete();
                    }
                }
            });
        }

        // candidate 经过一段时间的投票后，还没有成为 Leader，则视为选举失败，退回 Follower
        // 这个时长一般和选举超时时间一样
        vertx.setTimer(electionTimeout, id -> {
            promise.fail("node:" + this.nodeId + " election failed, fallback to follower");
        });

        promise.future()
                .onComplete(res -> {
                    becomeLeader();
                })
                .onFailure(res -> {
                    becomeFollow();
                });
    }


    public boolean processAppendEntriesRequest(Grpc.AppendEntriesRequest request) {
        if (raftLog.checkPre(request.getPreLogIndex(), request.getPreLogTerm())) {
            return false;
        }
        List<RaftLog.Entry> entries = request.getEntriesList().stream().map(RaftLog.Entry::parse).collect(Collectors.toList());
        raftLog.append(entries);
        return true;
    }

    /**
     * leader raft 节点处理来自客户端的请求
     * 1. 先将请求写入自己的日志
     * 2. 向其余节点广播 appendEntries
     * 3. 如果有过半节点收到 appendEntries 并成功返回，leader 则 apply 请求并相应客户端
     * <p>
     * 如果有节点返回 false，则说明其日志和 leader 有出入，对其进行日志恢复
     *
     * @param command
     */
    public void processCommandRequest(Command command) {
        RaftLog.Entry entry = raftLog.append(command);

        // peers 中不包含自己，过半数量直接取 peers.size() / 2，不用算自己
        AtomicInteger majority = new AtomicInteger(peers.size() / 2);
        Promise<Void> promise = Promise.promise();
        AtomicBoolean completeStatus = new AtomicBoolean(false);
        // 广播 appendEntries rpc
        // 其中附带 preLogIndex 和 preLogTerm 用于日志恢复
        broadcastAppendEntries(node -> {
            String peerId = node.getNodeId();
            long preLogIndex = nextLogIndexMap.get(peerId) - 1;
            return Grpc.AppendEntriesRequest
                    .newBuilder()
                    .setNodeId(nodeId)
                    .setTerm(currentTerm)
                    .setPreLogIndex(preLogIndex)
                    .setPreLogTerm(raftLog.getTermByIndex(preLogIndex))
                    .addAllEntries(List.of(entry.antiParse()))
                    .build();
        }).forEach(future -> {
            future.onSuccess(res -> {
                if (res.getSuccess()) {
                    if (majority.getAndDecrement() == 0 && completeStatus.compareAndSet(false, true)) {
                        promise.complete();
                    }
                } else {
                    // todo 对其单独开线程，执行日志恢复

                }
            });
        });

        promise.future().onComplete(res -> {
            // todo 根据一致性要求再改动
        });
    }

    /**
     * 给所有其他节点发送 appendEntries RPC
     *
     * @param requestAcquire 请求构造func
     * @return
     */
    public List<Future<Grpc.AppendEntriesResponse>> broadcastAppendEntries(Function<RaftNode, Grpc.AppendEntriesRequest> requestAcquire) {
        List<Future<Grpc.AppendEntriesResponse>> res = new ArrayList<>();
        for (RaftNode node : peers.values()) {
            RpcAddress peerAddress = node.getRpcAddress();
            res.add(rpcProxy.appendEntries(peerAddress, requestAcquire.apply(node)));
        }
        return res;
    }

    /**
     * 当一个节点失去 Leader 角色后，需要停止心跳
     */
    public void revokeHeartBeatPeriodic() {
        if (null != heartBeatPeriodicId) {
            vertx.cancelTimer(heartBeatPeriodicId);
        }
    }

    public void becomeFollow() {
        becomeFollow(currentTerm, null);
    }

    public void becomeFollow(long term, String leaderId) {
        revokeHeartBeatPeriodic();
        this.currentTerm = term;
        this.leaderId = leaderId;
        this.role = RoleEnum.Follower;
        this.votedFor = null;
        this.lastHeartBeat = System.currentTimeMillis();
    }

    public void becomeCandidate() {
        revokeHeartBeatPeriodic();
        this.role = RoleEnum.Candidate;
        this.leaderId = this.nodeId;
        this.votedFor = this.nodeId;
        this.currentTerm++;
        this.lastHeartBeat = System.currentTimeMillis();
        log.info("{} becomeCandidate, term:{}", nodeId, currentTerm);
    }

    public void becomeLeader() {
        log.info("{} becomeLeader, term:{}", nodeId, currentTerm);
        // 在节点发起选举的过程中，可能有其他节点已经成功成为了 Leader，而这个节点变为了 Follower
        // 对非 candidate 的直接拦截
        if (role != RoleEnum.Candidate) {
            return;
        }
        this.role = RoleEnum.Leader;
        this.leaderId = this.nodeId;
        this.nextLogIndexMap.replaceAll((key, value) -> raftLog.getNextLogIndex());
        this.lastHeartBeat = System.currentTimeMillis();
        vertx.setPeriodic(0, HEARTBEAT_INTERVAL, id -> {
            heartBeatPeriodicId = id;
            broadcastAppendEntries(node -> Grpc.AppendEntriesRequest.newBuilder().setNodeId(nodeId).setTerm(currentTerm).build())
                    .forEach(future -> {
                        future.onSuccess(
                                res -> {
                                    // 如果 leader 发现其他节点的 term 大于自己，需要重新将自己变更为 follower
                                    if (!res.getSuccess()) {
                                        becomeFollow(res.getTerm(), null);
                                    }
                                }
                        );
                    });
        });
    }


    /**
     * 重设选举过期时间
     */
    public void resetElectionTimeout() {
        electionTimeout = MIN_ELECTION_TIMEOUT + random.nextInt(MAX_ELECTION_TIMEOUT - MIN_ELECTION_TIMEOUT + 1);
    }

    /**
     * peers 转化为除了自己的所有节点
     * nextLogIndexMap value 初始化为下一个 logIndex
     *
     * @param map 所有节点
     */
    public void loadPeers(Map<String, RaftNode> map) {
        peers = map.entrySet().stream()
                .filter(entry -> !entry.getValue().getNodeId().equals(this.nodeId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        nextLogIndexMap = peers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> raftLog.getNextLogIndex()));
    }
}

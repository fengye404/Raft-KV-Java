package top.fengye.raft;

import io.vertx.core.AbstractVerticle;
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
            RpcAddress peerAddress = node.getRpcAddress();
            rpcProxy.applyVote(peerAddress, Grpc.ApplyVoteRequest.newBuilder().setNodeId(nodeId).setTerm(currentTerm).build())
                    .onSuccess(res -> {
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

    public void doPut(Command command, Promise<Void> promise) {

    }


    /**
     * Leader 节点需要定时广播给其他节点 appendEntries RPC
     */
    public void broadcastAppendEntries() {
        // 给所有其他节点发送 appendEntries RPC
        for (RaftNode node : peers.values()) {
            RpcAddress peerAddress = node.getRpcAddress();
            rpcProxy.appendEntries(peerAddress, Grpc.AppendEntriesRequest.newBuilder().setNodeId(nodeId).setTerm(currentTerm).build())
                    .onSuccess(res -> {
                        // 如果 leader 发现其他节点的 term 大于自己，需要重新将自己变更为 follower
                        if (!res.getSuccess()) {
                            becomeFollow(res.getTerm(), null);
                        }
                    });
        }
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
        this.lastHeartBeat = System.currentTimeMillis();
        vertx.setPeriodic(0, HEARTBEAT_INTERVAL, id -> {
            heartBeatPeriodicId = id;
            broadcastAppendEntries();
        });
    }


    /**
     * 重设选举过期时间
     */
    public void resetElectionTimeout() {
        electionTimeout = MIN_ELECTION_TIMEOUT + random.nextInt(MAX_ELECTION_TIMEOUT - MIN_ELECTION_TIMEOUT + 1);
    }

    /**
     * peers过滤掉自己
     *
     * @param map
     */
    public void setPeers(Map<String, RaftNode> map) {
        peers = map.entrySet().stream().filter(entry -> !entry.getValue().getNodeId().equals(this.nodeId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

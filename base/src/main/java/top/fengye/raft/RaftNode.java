package top.fengye.raft;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import top.fengye.rpc.Rpc;
import top.fengye.rpc.RpcAddress;
import top.fengye.rpc.RpcImpl;
import top.fengye.rpc.body.request.RequestVoteRequest;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: RaftNode
 */
@Getter
@Setter
public class RaftNode extends AbstractVerticle implements Serializable {

    private static final long serialVersionUID = -1;

    private String nodeId;

    private RaftLog raftLog;

    private long currentTerm;

    private String votedFor;

    private RoleEnum role;

    private Map<String, RaftNode> peers;

    private Map<String, Long> nextLogIndexMap;

    private RpcAddress rpcAddress;

    private Rpc rpc;

    /**
     * 最后一次心跳时间，用于选举计时器计算
     */
    private long lastHeatBeat;

    /**
     * 选举超时时长，每次选举后都会重置，范围为 150ms ~ 300ms
     */
    private long electionTimeout;

    /**
     * 心跳间隔
     */
    private static final int HEATBEAT_INTERVAL = 100;

    /**
     * 最小选举超时
     */
    private static final int MIN_ELECTION_TIMEOUT = 150;

    /**
     * 最大选举超时
     */
    private static final int MAX_ELECTION_TIMEOUT = 300;


    private final Random random = new Random();

    public RaftNode() {
        this.nodeId = UUID.randomUUID().toString();
        this.raftLog = new RaftLog();
        this.lastHeatBeat = System.currentTimeMillis();
        this.currentTerm = 0L;
        this.votedFor = null;
        this.role = RoleEnum.Follower;
        this.peers = new HashMap<>();
        this.nextLogIndexMap = new HashMap<>();
        this.rpc = new RpcImpl(this);
        resetElectionTimeout();
    }

    @Override
    public void start() throws Exception {
        rpc.initHandler();

        startElectionTimer();

        super.start();
    }


    /**
     * 选举计时器超时后，将发起选举，并重置自己的超时时间
     */
    public void startElectionTimer() {
        vertx.setPeriodic(electionTimeout, id -> {
            if (RoleEnum.Leader != role &&
                    currentTerm - lastHeatBeat > 0) {
                startElection();
                resetElectionTimeout();
            }
        });
    }

    public void startElection() {
        becomeCandidate();

        // 过半数量，实际上是 (peers.size() + 1) / 2 + 1
        // 但是自己默认给自己投票，所以需要 - 1
        AtomicInteger majority = new AtomicInteger((peers.size() + 1) / 2);
        Promise<Void> promise = Promise.promise();
        AtomicBoolean completeStatus = new AtomicBoolean(false);

        // 给所有其他节点发送投票信息
        for (RaftNode node : peers.values()) {
            RpcAddress peerAddress = node.getRpcAddress();
            rpc.requestVote(peerAddress, new RequestVoteRequest(this.currentTerm))
                    .onSuccess(res -> {
                        // 如果自己的 term 小于一个 peer 的 term，说明选期已经落后，直接变为 follower，以减少不必要的选举
                        if (res.getTerm() > currentTerm) {
                            becomeFollow(res.getTerm());
                        }
                        if (res.isAgreed()) {
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

    public void becomeLeader() {
        // 在节点发起选举的过程中，可能有其他节点已经成功成为了 Leader，而这个节点变为了 Follower
        // 对非 candidate 的直接拦截
        if (role != RoleEnum.Candidate) {
            return;
        }


    }

    public void becomeFollow() {
        becomeFollow(currentTerm);
    }

    public void becomeFollow(long term) {
        this.role = RoleEnum.Follower;
        this.votedFor = null;
        this.lastHeatBeat = System.currentTimeMillis();
    }

    public void becomeCandidate() {
        this.role = RoleEnum.Candidate;
        this.votedFor = this.nodeId;
        this.currentTerm++;
        this.lastHeatBeat = System.currentTimeMillis();
    }


    public void resetElectionTimeout() {
        electionTimeout = MIN_ELECTION_TIMEOUT + random.nextInt(MAX_ELECTION_TIMEOUT - MIN_ELECTION_TIMEOUT + 1);
    }
}

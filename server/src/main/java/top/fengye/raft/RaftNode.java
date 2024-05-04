package top.fengye.raft;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.Json;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import top.fengye.biz.Command;
import top.fengye.rpc.RpcAddress;
import top.fengye.rpc.RpcProxy;
import top.fengye.rpc.grpc.Grpc;
import top.fengye.rpc.grpc.GrpcProxyImpl;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
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
    private RaftFile raftFile;
    private int currentTerm;
    private String votedFor;
    private RoleEnum role;
    private Map<String, RaftNode> peers;
    private Map<String, Integer> nextLogIndexMap;
    private Map<String, Integer> matchLogIndexMap;
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
        this.lastHeartBeat = System.currentTimeMillis();
        this.currentTerm = 0;
        this.votedFor = null;
        this.role = RoleEnum.Follower;
        this.peers = new HashMap<>();
        this.nextLogIndexMap = new HashMap<>();
        this.rpcAddress = rpcAddress;
        this.raftLog = new RaftLog(this);
        this.raftStateMachine = new RaftStateMachine(this);
        resetElectionTimeout();
    }

    @Override
    public void start() throws Exception {
        this.rpcProxy = new GrpcProxyImpl(this);
        this.raftFile = new RaftFile(this);
        startElectionTimer();
        startHeatBeatTimer();
        // todo 日志持久化恢复相关逻辑
        raftFile.recover();
        super.start();
    }

    /**
     * 选举计时器超时后，将发起选举，并重置自己的超时时间
     */
    public void startElectionTimer() {
        vertx.setPeriodic(electionTimeout, id -> {
            if (RoleEnum.Leader != role &&
                    System.currentTimeMillis() - lastHeartBeat > electionTimeout) {
                resetElectionTimeout();
                startElection();
            }
        });
    }

    public void startHeatBeatTimer() {
        vertx.setPeriodic(HEARTBEAT_INTERVAL, id -> {
            if (RoleEnum.Leader == role) {
                broadcastAppendEntries();
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
                            .setLastLogIndex(raftLog.getCurrentLogIndex())
                            .setLastLogIndex(raftLog.getCurrentLogTerm())
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
            promise.tryFail("node:" + this.nodeId + " election failed, fallback to follower");
        });

        promise.future()
                .onSuccess(res -> {
                    becomeLeader();
                })
                .onFailure(res -> {
                    becomeFollow();
                });
    }


    public boolean processAppendEntriesRequest(Grpc.AppendEntriesRequest request) {
        if (raftLog.checkPre(request.getPreLogIndex(), request.getPreLogTerm())) {
            // leader 将 commitIndex 同步给 followers
            raftLog.setCommitIndex(request.getLeaderCommit());
            raftLog.apply(request.getLeaderCommit());
            // 如果没有附带 entriesList 则说明只是心跳请求
            if (CollectionUtils.isEmpty(request.getEntriesList())) {
                return true;
            }
            List<RaftLog.Entry> entries = request.getEntriesList().stream().map(RaftLog.Entry::parse).collect(Collectors.toList());
            raftLog.append(entries);
            log.info("{} received append entries from {}, data:{}", this.nodeId, request.getNodeId(), JSONObject.toJSONString(entries));
            return true;
        }
        return false;
    }

    /**
     * leader raft 节点处理来自客户端的请求
     * 分为两种情况：只读操作和写操作
     * Raft 的目标是为了确保写操作的一致性，因此写操作都需要走完整的 Raft 流程，即 appendLog、appendEntriesRPC
     * 对于写操作，正常流程如下：
     * 1. 先将请求写入自己的日志
     * 2. 向其余节点广播 appendEntries
     * 3. 如果有过半节点收到 appendEntries 并成功返回，leader 则将其标记为 commit 请求并响应客户端
     * <p>
     * <p>
     * 对于只读操作，可以不走上述流程，只读 Leader 的状态机。但是这里有两个点：
     * 1. 需要确定当前处理只读请求的节点的 Leader 身份
     * 2. 记录读取请求到达时的 commitIndex，需要保证这个 index 的 log 被 apply
     *
     * @param command
     */
    public void processCommandRequest(Command command, Promise<Grpc.CommandResponse> promise) {
        switch (command.getCommandType()) {
            case DEL:
            case PUT:
                writeLog(command, promise);
                break;
            case GET:
                readIndex(command, promise);
                break;
        }
    }

    /**
     * 线性一致读
     * 1. 记录当前的 commitIndex
     * 2. 发起一轮心跳，如果有过半的响应，则能确认自己的 Leader 身份
     * 3. 等待 readIndex 记录的 Log 被 apply 后，执行 read 请求
     */
    public void readIndex(Command command, Promise<Grpc.CommandResponse> promise) {
        int readIndex = raftLog.getCommitIndex();
        List<Future<Grpc.AppendEntriesResponse>> futures = broadcastAppendEntries();
        AtomicInteger count = new AtomicInteger(futures.size() / 2);
        Promise<Object> confirmLeader = Promise.promise();
        futures.forEach(res -> {
            if (count.decrementAndGet() == 0) {
                confirmLeader.tryComplete();
            }
        });
        confirmLeader.future()
                .onSuccess(res -> {
                            if (raftLog.getLastAppliedIndex() >= readIndex) {
                                promise.complete(raftStateMachine.doGet(command));
                            } else {
                                raftLog.getApplyEventQueue().add(Pair.of(readIndex, () ->
                                        promise.complete(raftStateMachine.doGet(command))
                                ));
                            }
                        }
                );
    }

    /**
     * 处理写请求
     */
    public void writeLog(Command command, Promise<Grpc.CommandResponse> promise) {
        raftLog.append(command, () -> promise.complete(
                        Grpc.CommandResponse.newBuilder()
                                .setSuccess(true)
                                .setResult("ok")
                                .build()
                )
        );
        broadcastAppendEntries();
    }


    /**
     * 给所有其他节点发送 appendEntries RPC
     *
     * @return
     */
    public List<Future<Grpc.AppendEntriesResponse>> broadcastAppendEntries() {
        List<Future<Grpc.AppendEntriesResponse>> res = new ArrayList<>();
        for (RaftNode node : peers.values()) {
            res.add(this.appendEntries(node));
        }
        return res;
    }

    public Future<Grpc.AppendEntriesResponse> appendEntries(RaftNode peer) {
        String peerId = peer.getNodeId();
        int myCurrentIndex = raftLog.getCurrentLogIndex();
        int peerNextIndex = nextLogIndexMap.get(peerId);
        int peerPreIndex = peerNextIndex - 1;
        Grpc.AppendEntriesRequest.Builder requestBuilder = Grpc.AppendEntriesRequest.newBuilder()
                .setNodeId(nodeId)
                .setTerm(currentTerm)
                .setPreLogIndex(peerPreIndex)
                .setPreLogTerm(raftLog.getTermByIndex(peerPreIndex))
                .setLeaderCommit(raftLog.getCommitIndex());
        if (peerNextIndex <= myCurrentIndex) {
            // 如果目标的 log 落后自己，则说明需要给其复制日志，截取 [peerNextIndex,myNextIndex)
            // 否则不用附带 entries 字段，目标收到后会将其视作心跳
            requestBuilder.addAllEntries(raftLog.slice(peerNextIndex).stream().map(RaftLog.Entry::antiParse).collect(Collectors.toList()));
        }

        return rpcProxy.appendEntries(peer.rpcAddress, requestBuilder.build())
                .onSuccess(res -> {
                    if (res.getSuccess()) {
                        matchLogIndexMap.put(peerId, myCurrentIndex);
                        nextLogIndexMap.put(peerId, myCurrentIndex + 1);
                        raftLog.reCalculateCommitIndex();
                    } else {
                        if (res.getTerm() > currentTerm) {
                            becomeFollow(res.getTerm(), null);
                        } else {
                            // 目标拒绝接收，说明目标日志和自己不一致，回退目标的 next
                            // 随着不断地 appendEntries，peerNextIndex 会不会断回退，直到和自己一致
                            nextLogIndexMap.put(peerId, peerNextIndex - 1);
                        }
                    }
                });
    }

    public void becomeFollow() {
        becomeFollow(currentTerm, null);
    }

    public void becomeFollow(int term, String leaderId) {
        this.currentTerm = term;
        this.leaderId = leaderId;
        this.role = RoleEnum.Follower;
        this.votedFor = null;
        this.lastHeartBeat = System.currentTimeMillis();
    }

    public void becomeCandidate() {
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
        this.nextLogIndexMap.replaceAll((key, value) -> raftLog.getCurrentLogIndex() + 1);
        this.lastHeartBeat = System.currentTimeMillis();
    }


    /**
     * 重设选举过期时间
     */
    public void resetElectionTimeout() {
        electionTimeout = MIN_ELECTION_TIMEOUT + random.nextInt(MAX_ELECTION_TIMEOUT - MIN_ELECTION_TIMEOUT + 1);
    }

    /**
     * peers 转化为除了自己的所有节点
     * nextLogIndexMap value 初始化为 nextLogIndex
     * matchLogIndexMap value 全部初始化为 0
     *
     * @param map 所有节点
     */
    public void loadPeers(Map<String, RaftNode> map) {
        peers = map.entrySet().stream()
                .filter(entry -> !entry.getValue().getNodeId().equals(this.nodeId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        nextLogIndexMap = peers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> raftLog.getNextLogIndex()));
        matchLogIndexMap = peers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, v -> 0));
    }
}

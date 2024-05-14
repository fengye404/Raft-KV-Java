package top.fengye;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import top.fengye.raft.RaftNode;
import top.fengye.rpc.RpcAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: FengYe
 * @date: 2024/5/5 上午4:41
 * @description: Server
 */
@Slf4j
public class Server {
    private static final ReentrantLock LOCK = new ReentrantLock();
    private static final Condition CONDITION = LOCK.newCondition();

    public static void main(String[] args) {
        CommandLine cliConfig = parseCliConfig(args);
        JSONObject config = parseNodeConfig(cliConfig.hasOption("config") ? cliConfig.getOptionValue("config") : "config.json");

        VertxOptions vertxOptions = new VertxOptions()
                .setBlockedThreadCheckInterval(10000000L)
                .setBlockedThreadCheckIntervalUnit(TimeUnit.DAYS)
                .setEventLoopPoolSize(1)
                .setWorkerPoolSize(1)
                .setInternalBlockingPoolSize(1);
        Vertx vertx = Vertx.vertx(vertxOptions);
        RaftNode raftNode = new RaftNode(config.getString("nodeId"), new RpcAddress(config.getString("host"), config.getInteger("port")));
        loadPeers(raftNode, config);
        if (cliConfig.hasOption("recover")) {
            raftNode.setRecover(Boolean.valueOf(cliConfig.getOptionValue("recover")));
        }
        vertx.deployVerticle(raftNode);

        // 阻塞主线程，直到程序关闭
        addHook();
        try {
            LOCK.lock();
            CONDITION.await();
        } catch (InterruptedException e) {
            log.error("", e);
        } finally {
            LOCK.unlock();
        }
    }

    public static CommandLine parseCliConfig(String[] args) {
        Options options = new Options();
        Option recover = new Option("r", "recover", true, "whether need to restore the status from an existing file or not");
        Option nodeConfigPath = new Option("c", "config", true, "node config file path");
        recover.setRequired(false);
        nodeConfigPath.setRequired(false);
        options.addOption(recover).addOption(nodeConfigPath);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            return cmd;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject parseNodeConfig(String nodeConfigPath) {
        JSONObject nodeConfig = new JSONObject();
        try {
            String fileContent = Files.readString(Path.of(nodeConfigPath));
            nodeConfig = JSONObject.parseObject(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nodeConfig;
    }

    public static void loadPeers(RaftNode raftNode, JSONObject jsonObject) {
        Map<String, RaftNode.BaseInfo> peersMap = new HashMap<>();
        JSONArray peers = jsonObject.getJSONArray("peers");
        for (int i = 0; i < peers.size(); i++) {
            JSONObject peer = peers.getJSONObject(i);
            peersMap.put(
                    peer.getString("nodeId"),
                    new RaftNode.BaseInfo(
                            peer.getString("nodeId"),
                            new RpcAddress(peer.getString("host"), peer.getInteger("port"))
                    )
            );
        }
        raftNode.loadPeers(peersMap);
    }

    public static void addHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOCK.lock();
                CONDITION.signal();
            } finally {
                LOCK.unlock();
            }
        }));
    }
}

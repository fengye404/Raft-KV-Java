package top.fengye;

import io.vertx.core.Future;
import io.vertx.core.cli.Argument;
import io.vertx.core.cli.CLI;
import io.vertx.core.cli.CommandLine;
import org.apache.commons.lang3.RandomUtils;
import top.fengye.biz.Command;
import top.fengye.biz.Value;
import top.fengye.rpc.RpcAddress;
import top.fengye.biz.Key;
import top.fengye.rpc.grpc.BizParam;
import top.fengye.rpc.grpc.Grpc;
import top.fengye.util.RpcUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author: FengYe
 * @date: 2024/5/1 上午3:49
 * @description: CommandProcessor 枚举单例
 */
public enum CommandProcessor {
    INSTANCE;


    private RpcUtils rpcUtils = new RpcUtils();
    private Set<RpcAddress> servers = new HashSet<>();
    private CLI parser;

    private static final Random random = new Random();


    {
        parser = CLI.create("command")
                .addArgument(new Argument()
                        .setIndex(0)
                        .setDescription("type")
                        .setArgName("type"))
                .addArgument(new Argument()
                        .setIndex(1)
                        .setDescription("key")
                        .setArgName("key"))
                .addArgument(new Argument()
                        .setIndex(2)
                        .setDescription("value")
                        .setArgName("value")
                        .setRequired(false));


        servers.add(new RpcAddress("localhost", 8080));
        servers.add(new RpcAddress("localhost", 8081));
        servers.add(new RpcAddress("localhost", 8082));
    }

    public String processRequest(List<String> request) {
        StringBuilder result = new StringBuilder();
        CommandLine parse = parser.parse(request);
        Command command = new Command();
        command.setCommandType(Command.CommandType.ofValue(parse.getArgumentValue("type")));
        command.setKey(Key.ofString(parse.getArgumentValue("key")));
        command.setValue(Value.ofString(Optional.ofNullable(parse.<String>getArgumentValue("value")).orElse("")));
        rpcRequest(randomServer(), command, result);
        return result.toString();
    }

    public void rpcRequest(RpcAddress server, Command command, StringBuilder result) {
        try {
            Grpc.CommandResponse res = block(rpcUtils.doRequest(server, command)).get();
            if (!res.getSuccess()) {
                if (res.getRedirect()) {
                    rpcRequest(new RpcAddress(res.getRedirectHost(), res.getRedirectPort()), command, result);
                } else {
                    rpcRequest(randomServer(), command, result);
                }
            } else {
                result.append(res.getResult());
            }
        } catch (Exception e) {
            result.append("目标节点宕机，请重试");
        }
    }

    private <T> CompletableFuture<T> block(Future<T> future) {
        return future.toCompletionStage().toCompletableFuture();
    }

    private RpcAddress randomServer() {
        List<RpcAddress> list = new ArrayList<>(servers);
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }
}

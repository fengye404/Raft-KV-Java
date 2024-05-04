package top.fengye.biz;

import com.google.protobuf.ByteString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.fengye.rpc.grpc.BizParam;

/**
 * @author: FengYe
 * @date: 2024/4/10 上午2:50
 * @description: 将 Command 与 grpc 的 bizParam 分开是为了解耦，同时定制化其结构
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Command {

    private CommandType commandType;
    private Key key;
    private Value value;

    public Command(BizParam.Command command) {
        this.key = new Key(command.getKey());
        this.value = new Value(command.getValue());
        this.commandType = CommandType.parse(command.getType());
    }

    public static Command parse(BizParam.Command command) {
        Command res = new Command();
        res.key = new Key(command.getKey());
        res.value = new Value(command.getValue());
        res.commandType = CommandType.parse(command.getType());
        return res;
    }

    public BizParam.Command antiParse() {
        return BizParam.Command.newBuilder()
                .setType(BizParam.CommandType.valueOf(this.commandType.name()))
                .setKey(BizParam.Key.newBuilder().setData(ByteString.copyFrom(this.key.getData())))
                .setValue(BizParam.Value.newBuilder().setData(ByteString.copyFrom(this.value.getData())))
                .build();
    }

    public enum CommandType {
        GET,
        PUT,
        DEL;

        public static CommandType parse(BizParam.CommandType commandType) {
            for (CommandType value : CommandType.values()) {
                if (value.name().equals(commandType.name())) {
                    return value;
                }
            }
            return null;
        }

        public static CommandType ofValue(String value) {
            if ("get".equals(value)) {
                return CommandType.GET;
            } else if ("put".equals(value)) {
                return CommandType.PUT;
            } else if ("del".equals(value)) {
                return CommandType.DEL;
            }
            return null;
        }
    }
}

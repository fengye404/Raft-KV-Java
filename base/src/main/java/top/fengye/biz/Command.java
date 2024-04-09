package top.fengye.biz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.fengye.rpc.grpc.BizParam;

import javax.annotation.Nullable;

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
    }
}

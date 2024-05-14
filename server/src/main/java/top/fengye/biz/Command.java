package top.fengye.biz;

import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import top.fengye.rpc.grpc.BizParam;

import java.nio.ByteBuffer;

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

    public static Command noop() {
        Command res = new Command();
        res.key = Key.noop();
        res.value = Value.noop();
        res.commandType = CommandType.NOOP;
        return res;
    }

    public BizParam.Command antiParse() {
        return BizParam.Command.newBuilder()
                .setType(BizParam.CommandType.valueOf(this.commandType.name()))
                .setKey(BizParam.Key.newBuilder().setData(ByteString.copyFrom(this.key.getData())))
                .setValue(BizParam.Value.newBuilder().setData(ByteString.copyFrom(this.value.getData())))
                .build();
    }

    /**
     * 将 command 转化为二进制
     * | commandType | keySize | key | valueSize | value |
     *
     * @return
     */
    public ByteBuffer toByteBuffer() {
        ByteBuffer allocate = ByteBuffer.allocate(4 + 4 + key.getData().length + 4 + value.getData().length);
        return allocate.putInt(commandType.getCode())
                .putInt(key.getData().length)
                .put(key.getData())
                .putInt(value.getData().length)
                .put(value.getData());
    }

    public Command(ByteBuf byteBuf) {
        commandType = CommandType.ofValue(byteBuf.readInt());
        int keySize = byteBuf.readInt();
        byte[] keyBytes = new byte[keySize];
        byteBuf.readBytes(keyBytes);
        key = new Key(keyBytes);
        int valueSize = byteBuf.readInt();
        byte[] valueBytes = new byte[valueSize];
        byteBuf.readBytes(valueBytes);
        value = new Value(valueBytes);
    }

    @Getter
    @AllArgsConstructor
    public enum CommandType {
        NOOP(-1),
        GET(1),
        PUT(2),
        DEL(3);

        private int code;

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

        public static CommandType ofValue(int value) {
            if (value == -1) {
                return CommandType.NOOP;
            } else if (value == 1) {
                return CommandType.GET;
            } else if (value == 2) {
                return CommandType.PUT;
            } else if (value == 3) {
                return CommandType.DEL;
            }
            return null;
        }
    }
}

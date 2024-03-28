package top.fengye.biz;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.nio.charset.Charset;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: Command
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Command {

    private Type type;

    private String key;

    private String value;

    private static final String split = "\0";

    public Command(Buffer buffer) {
        ByteBuf byteBuf = buffer.getByteBuf();
        this.type = Type.ofCode(byteBuf.readInt());
        String[] keyValue = byteBuf.toString(Charset.defaultCharset()).split(split);
        this.key = keyValue[0];
        this.value = keyValue[1];
    }

    public Buffer toBuffer() {
        Buffer buffer = Buffer.buffer();
        buffer.appendInt(type.code);
        buffer.appendString(key + split + value);
        return buffer;
    }

    @Getter
    public enum Type {
        GET(1),
        PUT(2);

        private final int code;

        Type(int code) {
            this.code = code;
        }

        public static Type ofCode(int code) {
            for (Type value : values()) {
                if (code == value.getCode()) {
                    return value;
                }
            }
            return null;
        }
    }
}

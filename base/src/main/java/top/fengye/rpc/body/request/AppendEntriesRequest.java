package top.fengye.rpc.body.request;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import top.fengye.biz.Command;

import java.nio.charset.Charset;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: AppendEntries RPC request body
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class AppendEntriesRequest {


    private Command command;

    public AppendEntriesRequest(Buffer buffer) {
        this.command = new Command(buffer);
    }

    public Buffer toBuffer() {
        Buffer buffer = Buffer.buffer();
        buffer.appendBuffer(command.toBuffer());
        return buffer;
    }

    public static void main(String[] args) {
        Command c1 = new Command();
        c1.setType(Command.Type.GET);
        c1.setKey("hello");
        c1.setValue("world");

        Buffer buffer = Buffer.buffer();
        buffer.appendLong(123L);
        buffer.appendBuffer(c1.toBuffer());


        ByteBuf byteBuf = buffer.getByteBuf();
        byteBuf.readLong();
        Command c2 = new Command(Buffer.buffer(byteBuf));
        System.out.println(c2);
    }
}

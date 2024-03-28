package top.fengye.rpc.body.request;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:42
 * @description: RequestVoteRequest RPC request body
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RequestVoteRequest {
    private long term;

    public RequestVoteRequest(Buffer buffer) {
        ByteBuf byteBuf = buffer.getByteBuf();
        this.term = byteBuf.readLong();
    }

    public Buffer toBuffer() {
        Buffer buffer = Buffer.buffer();
        buffer.appendLong(term);
        return buffer;
    }
}

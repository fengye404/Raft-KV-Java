package top.fengye.rpc.body.response;

import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:44
 * @description: RequestVoteResponse RPC response body
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RequestVoteResponse {
    private boolean agreed;

    private long term;

    public RequestVoteResponse(Buffer buffer) {
        ByteBuf byteBuf = buffer.getByteBuf();
        this.agreed = byteBuf.readByte() == 1;
        this.term = byteBuf.readLong();
    }

    public Buffer toBuffer() {
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(agreed ? (byte) 1 : (byte) 0);
        buffer.appendLong(term);
        return buffer;
    }
}

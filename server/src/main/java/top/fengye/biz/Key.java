package top.fengye.biz;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.fengye.rpc.grpc.BizParam;

/**
 * @author: FengYe
 * @date: 2024/4/10 上午2:50
 * @description: Key
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Key {
    private byte[] data;

    public static Key ofString(String key) {
        Key res = new Key();
        res.data = key.getBytes();
        return res;
    }

    public static Key noop() {
        Key res = new Key();
        res.data = new byte[]{};
        return res;
    }

    public Key(BizParam.Key key) {
        this.data = key.getData().toByteArray();
    }
}

package top.fengye.biz;

import lombok.Data;
import top.fengye.rpc.grpc.BizParam;

/**
 * @author: FengYe
 * @date: 2024/4/10 上午2:50
 * @description: Key
 */
@Data
public class Key {
    private byte[] data;

    public Key(BizParam.Key key) {
        this.data = key.getData().toByteArray();
    }
}

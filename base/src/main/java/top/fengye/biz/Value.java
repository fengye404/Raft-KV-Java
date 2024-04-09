package top.fengye.biz;

import lombok.Data;
import top.fengye.rpc.grpc.BizParam;

/**
 * @author: FengYe
 * @date: 2024/4/10 上午2:50
 * @description: Value
 */
@Data
public class Value {
    private byte[] data;

    public Value(BizParam.Value value) {
        this.data = value.getData().toByteArray();
    }
}

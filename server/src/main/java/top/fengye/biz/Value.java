package top.fengye.biz;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.fengye.rpc.grpc.BizParam;

/**
 * @author: FengYe
 * @date: 2024/4/10 上午2:50
 * @description: Value
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Value {
    private byte[] data;

    public static Value ofString(String value) {
        Value res = new Value();
        res.data = value.getBytes();
        return res;
    }

    public static Value noop() {
        Value res = new Value();
        res.data = new byte[]{};
        return res;
    }

    public Value(BizParam.Value value) {
        this.data = value.getData().toByteArray();
    }
}

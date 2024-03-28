package top.fengye.rpc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author: FengYe
 * @date: 2024/3/14 6:56
 * @description: RpcAddress
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RpcAddress {
    private String host;
    private int port;
}

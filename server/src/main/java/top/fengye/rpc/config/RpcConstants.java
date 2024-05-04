package top.fengye.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:09
 * @description: RpcConfig
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class RpcConstants {
//    public static final int raftPort = 4302;

    public static final String headerKeyNodeId = "nodeId";

    public static final String headerKeyTerm = "term";

    public static final String requestVotePath = "/requestVote";

    public static final String appendEntries = "/appendEntries";
}

package top.fengye.raft;

/**
 * @author: FengYe
 * @date: 2024/3/14 1:02
 * @description: RoleEnum
 */
public enum RoleEnum {
    Leader(1),
    Follower(2),
    Candidate(3);

    private int code;

    RoleEnum(Integer code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

package top.fengye.raft;

public enum RaftRoleEnum {
    Leader(1),
    Follower(2),
    Candidate(3);

    private Integer code;

    RaftRoleEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}

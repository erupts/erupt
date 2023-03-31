package xyz.erupt.flow.bean.entity.node;

import lombok.Data;

@Data
public class OaProcessNodeLeaderTop {

    private String endCondition;//TOP表示所有上级，LEAVE表示特定层级上级
    private Integer level;//只有top时有效，表示特定层级
}

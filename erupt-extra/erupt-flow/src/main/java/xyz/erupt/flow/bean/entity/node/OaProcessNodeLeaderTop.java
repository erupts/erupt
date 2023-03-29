package xyz.erupt.flow.bean.entity.node;

import lombok.Data;

import java.util.List;

@Data
public class OaProcessNodeLeaderTop {

    private String endCondition = "TOP";//TOP表示所有上级，LEAVE表示特定层级上级
    private Integer level = 1;//只有top时有效，表示特定层级
}

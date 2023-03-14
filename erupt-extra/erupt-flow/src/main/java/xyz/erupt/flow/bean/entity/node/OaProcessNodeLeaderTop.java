package xyz.erupt.flow.bean.entity.node;

import lombok.Data;

import java.util.List;

@Data
public class OaProcessNodeLeaderTop {

    private String endCondition = "TOP";
    private Integer level = 0;//为TOP时，表示最高层级(0表示不设限)，不为TOP时，表示特定层级
}

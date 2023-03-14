package xyz.erupt.flow.bean.entity.node;

import lombok.Data;

import java.util.List;

@Data
public class OaProcessNodeLeaderTop {

    private String endCondition = "TOP";
    private Integer endLevel = 1;
    private Integer level = 1;
}

package xyz.erupt.flow.bean.entity;

import lombok.Data;

import java.util.List;

@Data
public class OaProcessNodeGroup {
    String[] cids;
    String groupType;
    List<OaProcessNodeCondition> conditions;
}

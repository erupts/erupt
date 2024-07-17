package xyz.erupt.flow.bean.entity.node;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OaProcessNodeGroup {
    String[] cids;
    String groupType;
    List<OaProcessNodeCondition> conditions;
}

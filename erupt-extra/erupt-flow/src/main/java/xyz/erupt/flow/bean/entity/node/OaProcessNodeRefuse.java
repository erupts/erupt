package xyz.erupt.flow.bean.entity.node;

import lombok.Data;
import xyz.erupt.flow.constant.FlowConstant;

@Data
public class OaProcessNodeRefuse {
    String type = FlowConstant.REFUSE_TO_END;//驳回规则 TO_END  TO_NODE  TO_BEFORE
    String target;//驳回到指定ID的节点
}

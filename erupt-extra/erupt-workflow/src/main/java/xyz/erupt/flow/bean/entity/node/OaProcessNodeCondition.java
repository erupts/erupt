package xyz.erupt.flow.bean.entity.node;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OaProcessNodeCondition {
    String id;
    String title;
    String[] value;
    String compare;
    String valueType;
}

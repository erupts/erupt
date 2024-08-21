package xyz.erupt.flow.bean.entity.node;

import lombok.Data;

@Data
public class OaProcessNodeCondition {
    String id;
    String title;
    String[] value;
    String compare;
    String valueType;
}

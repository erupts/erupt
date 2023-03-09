package xyz.erupt.flow.bean.entity;

import lombok.Data;

import java.util.List;

@Data
public class OaProcessNodeProps {
    List<OaProcessNodeGroup> groups;
    String expression;
    String groupsType;
}

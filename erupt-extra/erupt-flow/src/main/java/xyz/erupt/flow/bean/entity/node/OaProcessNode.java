package xyz.erupt.flow.bean.entity.node;

import lombok.Data;

import java.util.List;

@Data
public class OaProcessNode {

    private String id;

    private String desc;

    private String name;

    private String type;

    private OaProcessNodeProps props;

    OaProcessNode children;

    private List<OaProcessNode> branchs;
}

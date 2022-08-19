package xyz.erupt.workflow.model;

import xyz.erupt.annotation.Erupt;
import xyz.erupt.jpa.model.MetaModelUpdateVo;
import xyz.erupt.workflow.annotation.EruptWorkflow;

/**
 * @author YuePeng
 * date 2022/8/17 22:12
 */
@EruptWorkflow
@Erupt(name = "工作流定义实体")
public class WorkFlowModel extends MetaModelUpdateVo {

    private String code;

    private String name;


}

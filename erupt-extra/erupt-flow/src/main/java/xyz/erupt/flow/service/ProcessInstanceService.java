package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaProcessDefinition;
import xyz.erupt.flow.bean.entity.OaProcessInstance;

public interface ProcessInstanceService extends IService<OaProcessInstance> {

    long countByProcessDefinitionId(String procDefId);

    long countByFormId(Long formId);

    /**
     * 创建一个新实例
     * @param processDefinition 流程定义
     * @param content 表单内容
     * @return
     */
    public OaProcessInstance newProcessInstance(OaProcessDefinition processDefinition, String content);

    /**
     * 完成
     * @param processInstId
     */
    void finish(Long processInstId);

}

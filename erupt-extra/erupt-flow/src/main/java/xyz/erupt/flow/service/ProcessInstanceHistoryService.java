package xyz.erupt.flow.service;

import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.OaProcessInstanceHistory;

public interface ProcessInstanceHistoryService {

    /**
     * 从流程实例拷贝得到对象
     *
     * @param procInst
     * @return
     */
    public OaProcessInstanceHistory copyAndSave(OaProcessInstance procInst);
}

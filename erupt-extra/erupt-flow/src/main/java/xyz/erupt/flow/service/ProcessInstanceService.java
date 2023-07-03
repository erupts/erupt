package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaProcessDefinition;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.OaProcessInstanceHistory;

import java.util.List;

public interface ProcessInstanceService extends IService<OaProcessInstance>, WithListener {

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

    /**
     * 流程终止
     * @param instId
     * @param remarks
     */
    void stop(Long instId, String remarks);

    /**
     * 查询与我相关的流程
     * 即我处理过的，或者抄送我的
     * @return
     */
    List<OaProcessInstanceHistory> getMineAbout(String keywords, int pageNum, int pageSize);

    /**
     * 更新表单数据
     * @param processInstId
     * @param content
     */
    void updateDataById(Long processInstId, String content);
}

package xyz.erupt.flow.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;

import java.io.Serializable;

public interface ProcessExecutionService extends IService<OaProcessExecution> {

    /**
     * 新建线程
     * @param defId
     * @param instanceId
     * @param currentNode
     * @param parent
     * @return
     */
    public OaProcessExecution newExecution(String defId, Long instanceId, JSONObject formContent, OaProcessNode currentNode, OaProcessExecution parent);

    /**
     * 触发线程的完成
     * @param executionId
     */
    public void triggerComplete(Long executionId);

    void removeByProcessInstId(Long id);

    /**
     * 触发线程继续
     * @param parentId
     */
    void triggerActive(Long parentId);

    /**
     * 结束流程
     * @param execution
     */
    void finish(OaProcessExecution execution);

    /**
     * 刷新节点json
     * @param id
     * @param toJSONString
     */
    void freshProcess(Long id, String toJSONString);
}

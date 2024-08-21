package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;

public interface ProcessExecutionService extends IService<OaProcessExecution>, WithListener {

    /**
     * 新建线程
     * @param defId
     * @param instanceId
     * @param currentNode
     * @param parent
     * @return
     */
    public OaProcessExecution newExecution(String defId, Long instanceId, OaProcessNode currentNode, OaProcessExecution parent);

    /**
     * 线程继续向前
     * @param executionId
     */
    public void step(Long executionId, OaProcessNode currentNode);

    void removeByProcessInstId(Long id);

    /**
     * 触发线程继续
     * @param parentId
     */
    void active(Long parentId);

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

    /**
     * 中断所有线程
     * @param instId
     * @param reason
     */
    void stopByInstId(Long instId, String reason);

    /**
     * 启动新线程
     * @param inst
     */
    OaProcessExecution newExecutionForInstance(OaProcessInstance inst);
}

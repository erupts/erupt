package xyz.erupt.flow.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;

import java.io.Serializable;
import java.util.List;

public interface ProcessActivityService extends IService<OaProcessActivity> {

    /**
     * 针对当前node创建节点
     * 如果当前节点不是用户任务，将会继续向前，直到下一个（或多个）用户任务
     * 然后将这些用户任务解析为节点，同时生成任务
     * @param execution
     * @param node
     * @return
     */
    public int newActivities(OaProcessExecution execution, JSONObject formContent, OaProcessNode node, String status);

    public int newActivities(OaProcessExecution execution, JSONObject formContent, OaProcessNode node);

    public void removeByProcessInstId(Long procInstId);

    public boolean activeByExecutionId(Long executionId);

    /**
     * 完成节点
     * @param activityId
     */
    void triggerComplete(Long activityId);

    /**
     * 查询当前线程的节点
     * @param executionId
     * @return
     */
    OaProcessActivity getByExecutionId(Long executionId);

    /**
     * 选择一个符合条件的节点，用于互斥分支
     * @param formContent
     * @param nodes
     * @return
     */
    OaProcessNode switchNode(JSONObject formContent, List<OaProcessNode> nodes);
}

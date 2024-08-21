package xyz.erupt.flow.bean.vo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import xyz.erupt.flow.bean.entity.OaTask;

/**
 * 除了任务信息外还有
 * 1，流程定义的 表单信息（OaProcessDefinition.formItems）
 * 2，流程实例的 表单内容（OaProcessInstance.formItems）
 * 3，当前线程的 节点配置（OaProcessExecution.process）
 */
@Data
public class TaskDetailVo extends OaTask {

    /**
     * 表单定义
     */
    private JSONArray formItems;
    /**
     * 表单内容
     */
    private JSONObject formData;
    /**
     * 节点配置
     */
    private JSONObject nodeConfig;
}

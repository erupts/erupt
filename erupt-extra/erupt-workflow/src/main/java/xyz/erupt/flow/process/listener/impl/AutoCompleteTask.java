package xyz.erupt.flow.process.listener.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.process.listener.AfterCreateTaskListener;
import xyz.erupt.flow.service.TaskService;

/**
 * 创建后触发，自动完成某些任务
 */
@Component
public class AutoCompleteTask implements AfterCreateTaskListener {

    @Override
    public int sort() {
        return 0;
    }

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(OaTask task) {
        if(FlowConstant.NODE_TYPE_CC.equals(task.getTaskType())) {
            taskService.complete(task.getId(), null, "抄送节点自动完成", "抄送节点自动完成", null);
        }
    }
}

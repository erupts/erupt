package xyz.erupt.flow.process.listener.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.process.listener.AfterActiveActivityListener;
import xyz.erupt.flow.service.TaskService;

/**
 * 活动激活后，激活所在的任务
 */
@Component
public class ActiveTasks implements AfterActiveActivityListener {

    @Override
    public int sort() {
        return 0;
    }

    @Autowired
    private TaskService taskService;

    @Override
    public void execute(OaProcessActivity executableNode) {
        taskService.activeTaskByActivityId(executableNode.getId());
    }

}

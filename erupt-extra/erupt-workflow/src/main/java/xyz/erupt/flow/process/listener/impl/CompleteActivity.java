package xyz.erupt.flow.process.listener.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.process.listener.AfterCompleteTaskListener;
import xyz.erupt.flow.service.ProcessActivityService;
import xyz.erupt.flow.service.TaskService;

/**
 * 任务完成后，尝试完成所在的活动
 */
@Component
public class CompleteActivity implements AfterCompleteTaskListener {
    @Override
    public int sort() {
        return 0;
    }

    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessActivityService processActivityService;

    @Override
    public void execute(OaTask task) {
        //尝试激活下一个任务，如果激活成功则不继续执行
        boolean a = taskService.activeTaskByActivityId(task.getActivityId());
        if(a) {
            return;
        }
        processActivityService.complete(task.getActivityId());
    }
}

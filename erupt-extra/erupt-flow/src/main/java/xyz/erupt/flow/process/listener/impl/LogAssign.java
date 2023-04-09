package xyz.erupt.flow.process.listener.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.process.listener.AfterAssignTaskListener;

@Component
@Slf4j
public class LogAssign implements AfterAssignTaskListener {
    @Override
    public int sort() {
        return 0;
    }

    @Override
    public void execute(OaTask task) {
        log.info("任务被转办，所属人{}，审批人。", task.getTaskOwner(), task.getAssignee());
    }
}

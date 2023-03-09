package xyz.erupt.flow.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.service.TaskService;

import java.util.List;

@Component
@Comment("完成任务")
public class TaskCompleteHandler implements OperationHandler<OaTask, Void> {

    @Autowired
    private TaskService taskService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String exec(List<OaTask> datas, Void unused, String[] param) {
        datas.forEach(data -> taskService.complete(data.getId(), "超管完成"));
        return null;
    }
}

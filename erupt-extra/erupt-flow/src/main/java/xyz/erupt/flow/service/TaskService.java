package xyz.erupt.flow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaTask;

import java.io.Serializable;
import java.util.List;

public interface TaskService extends IService<OaTask> {

    public List<OaTask> generateTask(OaProcessActivity activity);

    /**
     * 完成任务
     * @param taskId
     */
    public void complete(Long taskId, String remarks);

    /**
     * 查询当前流程的待完成任务
     * @param executionId
     */
    public List<OaTask> listByExecutionId(Long executionId);

    /**
     * 激活任务
     * @param id
     */
    void activeTask(Long id);

    /**
     * 分页查询我的任务
     * @param keywords
     * @return
     */
    List<OaTask> listMyTasks(String keywords);

    void removeByProcessInstId(Serializable id);

    void activeTaskByExecutionId(Long executionId);

    List<OaTask> listByActivityId(Long activityId);
}

package xyz.erupt.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskOperation;
import xyz.erupt.flow.mapper.OaTaskMapper;
import xyz.erupt.flow.service.ProcessActivityService;
import xyz.erupt.flow.service.TaskHistoryService;
import xyz.erupt.flow.service.TaskOperationService;
import xyz.erupt.flow.service.TaskService;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class TaskServiceImpl extends ServiceImpl<OaTaskMapper, OaTask>
        implements TaskService, DataProxy<OaTask> {

    @Autowired
    private TaskHistoryService taskHistoryService;
    @Autowired
    private TaskOperationService taskOperationService;
    @Autowired
    private EruptUserService eruptUserService;
    @Autowired
    private ProcessActivityService processActivityService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OaTask> generateTask(OaProcessActivity activity) {
        //创建任务，目前全部当作单任务处理
        OaTask build = OaTask.builder()
                .activityId(activity.getId())
                .activityKey(activity.getActivityKey())
                .executionId(activity.getExecutionId())
                .processInstId(activity.getProcessInstId())
                .processDefId(activity.getProcessDefId())
                .taskName(activity.getActivityName())
                .taskOwner(null)
                .assignee(null)
                .createDate(new Date())
                .taskDesc(activity.getDescription())
                .active(activity.getActive())
                .finished(false)
                .completeMode(activity.getCompleteMode())
                .completeSort(1)
                .taskOwner("erupt")//测试代码，先把所有任务分配给超管
                .build();
        super.save(build);
        taskHistoryService.copyAndSave(build);//保存历史
        return Arrays.asList(build);
    }

    /**
     * 完成任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(Long taskId, String remarks) {
        //查询任务，并进行判断
        OaTask task = this.getById(taskId);
        if(task==null) {
            throw new EruptApiErrorTip("任务"+taskId+"不存在");
        }
        if(!task.getActive()) {
            throw new EruptApiErrorTip("任务未激活，不可完成");
        }
        if(task.getFinished()) {
            throw new EruptApiErrorTip("任务已完成");
        }
        //任务标记为完成
        EruptUser currentEruptUser = eruptUserService.getCurrentEruptUser();
        task.setFinished(true);
        task.setFinishDate(new Date());
        task.setFinishUser(currentEruptUser.getAccount());
        task.setFinishUserName(currentEruptUser.getName());
        //变更历史表
        taskHistoryService.copyAndSave(task);
        //删除运行时表
        this.removeById(taskId);
        //记录日志
        taskOperationService.log(task, OaTaskOperation.COMPLETE, remarks);
        //触发活动的完成
        processActivityService.triggerComplete(task.getActivityId());
    }

    @Override
    public List<OaTask> listByExecutionId(Long executionId) {
        QueryWrapper<OaTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaTask::getExecutionId, executionId);
        queryWrapper.lambda().orderByAsc(OaTask::getCompleteSort);//按照执行顺序排序
        return this.list(queryWrapper);
    }

    @Override
    public void activeTask(Long taskId) {
        OaTask build = OaTask.builder()
                .active(true)
                .build();
        build.setId(taskId);
        this.updateById(build);
    }

    @Override
    public List<OaTask> listMyTasks(String keywords) {
        MPJLambdaWrapper<OaTask> queryWrapper = new MPJLambdaWrapper<OaTask>()
                .leftJoin(OaProcessInstance.class, OaProcessInstance::getId, OaTask::getProcessInstId)//关联流程实例
                .selectAll(OaTask.class)//查询任务全部字段
                .selectAs(OaProcessInstance::getBusinessTitle, OaTask::getBusinessTitle)
                .selectAs(OaProcessInstance::getFormName, OaTask::getFormName)
                .eq(OaTask::getActive, true);//只查询激活的任务
        if(StringUtils.isNotEmpty(keywords)) {
            queryWrapper.and(wrapper -> {
                wrapper.or().like(OaTask::getTaskName, keywords);
                wrapper.or().like(OaTask::getTaskDesc, keywords);
                wrapper.or().like(OaTask::getBusinessTitle, keywords);
                wrapper.or().like(OaTask::getFormName, keywords);
            });
        }
        String userName = "erupt";
        queryWrapper.and(wrapper -> {
            wrapper.or(orWrapper -> {//任务的所属人是我
                orWrapper.eq(OaTask::getTaskOwner, userName);
            });
            wrapper.or(orWrapper -> {//任务指派人是我
                orWrapper.isNull(OaTask::getTaskOwner);
                orWrapper.eq(OaTask::getAssignee, userName);
            });
            //其他2种，候选人包括我，或者候选角色包括我的角色
        });
        queryWrapper.orderByDesc(OaTask::getCreateDate);
        List<OaTask> list = this.baseMapper.selectJoinList(OaTask.class, queryWrapper);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByProcessInstId(Serializable procInstId) {
        QueryWrapper<OaTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaTask::getProcessInstId, procInstId);
        this.remove(queryWrapper);
    }

    @Override
    public void activeTaskByExecutionId(Long executionId) {
        QueryWrapper<OaTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaTask::getExecutionId, executionId);

        this.update(
                OaTask.builder()
                        .active(true)
                        .build()
                , queryWrapper
        );
    }

    @Override
    public List<OaTask> listByActivityId(Long activityId) {
        QueryWrapper<OaTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaTask::getActivityId, activityId);
        queryWrapper.lambda().orderByAsc(OaTask::getCompleteSort);
        return this.list(queryWrapper);
    }
}

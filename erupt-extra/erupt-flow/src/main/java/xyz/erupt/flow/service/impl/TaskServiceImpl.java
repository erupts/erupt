package xyz.erupt.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.flow.bean.entity.*;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.bean.vo.TaskDetailVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.mapper.OaTaskMapper;
import xyz.erupt.flow.process.engine.ProcessHelper;
import xyz.erupt.flow.process.listener.*;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;
import xyz.erupt.flow.service.*;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
public class TaskServiceImpl extends ServiceImpl<OaTaskMapper, OaTask> implements TaskService, DataProxy<OaTask> {

    private Map<Class<ExecutableNodeListener>, List<ExecutableNodeListener>> listenerMap = new HashMap<>();

    @Autowired
    private TaskHistoryService taskHistoryService;
    @Autowired
    private TaskOperationService taskOperationService;
    @Autowired
    private EruptUserService eruptUserService;
    @Lazy
    @Autowired
    private ProcessActivityService processActivityService;
    @Lazy
    @Autowired
    private TaskUserLinkService taskUserLinkService;
    @Lazy
    @Autowired
    private ProcessExecutionService processExecutionService;
    @Autowired
    private UserLinkServiceHolder userLinkService;
    @Lazy
    @Autowired
    private ProcessDefinitionService processDefinitionService;
    @Lazy
    @Autowired
    private ProcessInstanceService processInstanceService;
    @Lazy
    @Autowired
    private ProcessInstanceHistoryService processInstanceHistoryService;
    @Autowired
    private ProcessHelper processHelper;

    /**
     * 完成任务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaTask complete(Long taskId, String remarks, String content) {
        EruptUser currentEruptUser = eruptUserService.getCurrentEruptUser();
        OaTask task = this.complete(taskId, currentEruptUser.getAccount(), currentEruptUser.getName(), remarks, content);
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OaTask complete(Long taskId, String account, String accountName, String remarks, String content) {
        OaTask task = this.finish(OaTaskOperation.COMPLETE, taskId, account, accountName, remarks);
        //更新表单
        if(!StringUtils.isBlank(content)) {
            processInstanceService.updateDataById(task.getProcessInstId(), content);
        }
        //触发活动的完成
        this.listenerMap.get(AfterCompleteTaskListener.class).forEach(l -> l.execute(task));
        return task;
    }

    /**
     * 指派
     * @param taskId
     * @param users
     * @param remarks
     */
    @Override
    public void assign(Long taskId, Set<OrgTreeVo> users, String remarks) {
        //查询任务，并进行判断
        OaTask task = this.getById(taskId);
        if(task==null) {
            throw new EruptApiErrorTip("任务"+taskId+"不存在");
        }
        if(!task.getActive()) {
            throw new EruptApiErrorTip("任务未激活，不可转办");
        }
        if(task.getFinished()) {
            throw new EruptApiErrorTip("任务已完成");
        }
        //然后添加新的审批人
        this.setAssigns(task, users);
        //记录日志
        taskOperationService.log(task, OaTaskOperation.ASSIGN, remarks);

        //触发后置监听
        this.listenerMap.get(AfterAssignTaskListener.class).forEach(l -> l.execute(task));
    }

    /**
     * 设置处理人
     * @param task
     * @param userIds
     */
    private void setAssigns(OaTask task, Set<OrgTreeVo> userIds) {
        //移除所有的 持有人，审批人，候选人
        this.cleanAllAssigns(task);

        if(userIds.size()==1) {//只有一人，设置到处理人，否则设置到候选人
            OaTask build = OaTask.builder()
                    .assignee(userIds.iterator().next().getId())
                    .build();
            build.setId(task.getId());
            this.updateById(build);
            taskHistoryService.copyAndSave(build);
        }else {
            List<OaTaskUserLink> links = new ArrayList<>();
            while (userIds.iterator().hasNext()) {
                OaTaskUserLink link = new OaTaskUserLink();
                OrgTreeVo next = userIds.iterator().next();
                link.setUserLinkType(FlowConstant.USER_LINK_USERS);
                link.setLinkId(next.getId());
                link.setLinkName(next.getName());
                links.add(link);
            }
            taskUserLinkService.saveBatch(links);
        }
    }

    /**
     * 清空处理人
     * @param task
     */
    private void cleanAllAssigns(OaTask task) {
        task.setTaskOwner(null);
        task.setClaimDate(null);
        task.setAssignee(null);
        this.updateById(task);
        taskHistoryService.copyAndSave(task);
        //这里暂时不能删，考虑加个历史表
        //taskUserLinkService.removeByTaskId(task.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long taskId, String remarks, String content) {
        EruptUser currentEruptUser = eruptUserService.getCurrentEruptUser();
        this.refuse(taskId, currentEruptUser.getAccount(), currentEruptUser.getName(), remarks, content);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refuse(Long taskId, String account, String accountName, String remarks, String content) {
        //先完成当前任务
        OaTask task = this.finish(OaTaskOperation.REFUSE, taskId, account, accountName, remarks);
        //更新表单
        if(!StringUtils.isBlank(content)) {
            processInstanceService.updateDataById(task.getProcessInstId(), content);
        }
        //进行拒绝
        processHelper.refuse(task, accountName);
        //触发拒绝后置事件
        this.listenerMap.get(AfterRefuseTaskListener.class).forEach(l -> l.execute(task));
    }

    /**
     * 任务结束
     * @param taskId
     * @param account
     * @param accountName
     * @param remarks
     */
    private OaTask finish(String action, Long taskId, String account, String accountName, String remarks) {
        //查询任务，并进行判断
        OaTask task = this.getById(taskId);
        if(task==null) {
            throw new EruptApiErrorTip("任务"+taskId+"不存在");
        }
        if(!task.getActive()) {
            throw new EruptApiErrorTip("任务未激活，禁止操作");
        }
        if(task.getFinished()) {
            throw new EruptApiErrorTip("任务已完成");
        }
        //任务标记为完成
        task.setFinished(true);
        task.setActive(false);
        task.setFinishDate(new Date());
        task.setFinishUser(account);
        task.setFinishUserName(accountName);
        //变更历史表
        taskHistoryService.copyAndSave(task);
        //删除运行时表
        this.removeById(taskId);
        //记录日志
        taskOperationService.log(task, action, remarks);
        return task;
    }

    /**
     * 查询开始节点，开始节点只能有一个
     * @param activityId
     * @return
     */
    @Override
    public List<OaTask> getTasksByActivityId(Long activityId, String... types) {
        QueryWrapper<OaTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaTask::getActivityId, activityId);
        queryWrapper.lambda().in(OaTask::getActivityKey, types);
        queryWrapper.lambda().orderByAsc(OaTask::getCompleteSort);//按照执行顺序排序
        return this.list(queryWrapper);
    }

    @Override
    public List<OaTask> listMyTasks(String keywords) {
        MPJLambdaWrapper<OaTask> queryWrapper = new MPJLambdaWrapper<OaTask>()
                .leftJoin(OaProcessInstance.class, OaProcessInstance::getId, OaTask::getProcessInstId)//关联流程实例
                .leftJoin(OaProcessDefinition.class, OaProcessDefinition::getId, OaTask::getProcessDefId)//关联流程定义
                .selectAll(OaTask.class)//查询任务全部字段
                .selectAs(OaProcessDefinition::getLogo, OaTask::getLogo)
                .selectAs(OaProcessInstance::getCreator, OaTask::getInstCreator)
                .selectAs(OaProcessInstance::getCreatorName, OaTask::getInstCreatorName)
                .selectAs(OaProcessInstance::getCreateDate, OaTask::getInstCreateDate)
                .select(OaProcessInstance::getBusinessTitle)
                .select(OaProcessInstance::getFormName);
        //只查询激活的任务
        queryWrapper.eq(OaTask::getActive, true);
        //关键字筛选
        if(StringUtils.isNotEmpty(keywords)) {
            queryWrapper.and(wrapper -> {
                wrapper.like(OaTask::getTaskName, keywords)
                        .or().like(OaTask::getTaskDesc, keywords)
                        .or().like(OaProcessInstance::getBusinessTitle, keywords)
                        .or().like(OaProcessInstance::getFormName, keywords);
            });
        }
        //我的任务
        String userName = eruptUserService.getCurrentAccount();
        queryWrapper.and(wrapper -> {
            wrapper.or(orWrapper -> {//任务的所属人是我
                orWrapper.eq(OaTask::getTaskOwner, userName);
            });
            wrapper.or(orWrapper -> {//任务指派人是我
                orWrapper.isNull(OaTask::getTaskOwner);
                orWrapper.eq(OaTask::getAssignee, userName);
            });
            // 其他2种，候选人包括我，或候选角色包括我的角色
            //查询本人为候选人的情况
            List<OaTaskUserLink> links = taskUserLinkService.listByUserIds(Arrays.asList(userName));
            LinkedHashSet<String> roleIds = userLinkService.getRoleIdsByUserId(userName);
            if(!CollectionUtils.isEmpty(roleIds)) {
                //查询本角色为候选角色的情况
                links.addAll(taskUserLinkService.listByRoleIds(roleIds));
            }
            if(!CollectionUtils.isEmpty(links)) {//如果有数据，则作为条件查询
                wrapper.or(orWrapper -> {
                    orWrapper.isNull(OaTask::getTaskOwner);
                    orWrapper.isNull(OaTask::getAssignee);
                    orWrapper.in(OaTask::getId, links.stream().map(l -> l.getTaskId()).collect(Collectors.toSet()));
                });
            }

        });
        queryWrapper.orderByDesc(OaTask::getCreateDate);
        List<OaTask> list = this.baseMapper.selectJoinList(OaTask.class, queryWrapper);
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByProcessInstId(Long procInstId) {
        QueryWrapper<OaTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaTask::getProcessInstId, procInstId);
        this.remove(queryWrapper);
    }

    @Override
    public boolean activeTaskByActivityId(Long activityId) {
        List<OaTask> oaTasks = this.listByActivityId(activityId, false);
        for (int i = 0; i < oaTasks.size(); i++) {
            OaTask t = oaTasks.get(i);
            t.setActive(true);
            this.updateById(t);
            if(OaProcessActivity.SERIAL.equals(t.getCompleteMode())) {//串行只能激活一个
                break;
            }
        }
        //一次性触发所有监听器，这里先不在循环里触发
        for (int i = 0; i < oaTasks.size(); i++) {
            OaTask t = oaTasks.get(i);
            this.listenerMap.get(AfterActiveTaskListener.class).forEach(l -> l.execute(t));
            if(OaProcessActivity.SERIAL.equals(t.getCompleteMode())) {//串行只能激活一个
                break;
            }
        }
        return oaTasks.size()>0;
    }

    @Override
    public void saveBatchWithUserLink(List<OaTask> oaTasks) {
        oaTasks.forEach(this::save);
        taskHistoryService.copyAndSave(oaTasks);

        oaTasks.forEach(t -> {
            t.getUserLinks().forEach(link -> link.setTaskId(t.getId()));
            taskUserLinkService.saveBatch(t.getUserLinks());
        });
        //这里需要考虑一个问题，就是其中一个任务的监听导致其他任务不存在的情况
        oaTasks.forEach(t -> {
            //触发后置事件
            this.listenerMap.get(AfterCreateTaskListener.class).forEach(l -> l.execute(t));
            //触发后置事件
            this.listenerMap.get(AfterActiveTaskListener.class).forEach(l -> {
                if(t.getActive()) {
                    l.execute(t);
                }
            });
        });
    }

    @Override
    public TaskDetailVo getTaskDetail(Long taskId) {
        OaTask task = this.getById(taskId);
        if(task==null) {
            throw new EruptApiErrorTip("任务不存在或已被处理");
        }
        //创建一个vo，直接拷贝属性
        TaskDetailVo taskDetail = new TaskDetailVo();
        BeanUtils.copyProperties(task, taskDetail);
        //再查询其他属性
        OaProcessDefinition def = processDefinitionService.getById(task.getProcessDefId());
        taskDetail.setFormItems(JSON.parseArray(def.getFormItems()));//表单配置
        OaProcessInstance inst = processInstanceService.getById(task.getProcessInstId());
        taskDetail.setFormData(JSON.parseObject(inst.getFormItems()));//表单内容
        taskDetail.setInstCreator(inst.getCreator());
        taskDetail.setInstCreatorName(inst.getCreatorName());
        taskDetail.setInstCreateDate(inst.getCreateDate());
        taskDetail.setBusinessTitle(inst.getBusinessTitle());
        taskDetail.setFormName(inst.getFormName());
        OaProcessExecution execution = processExecutionService.getById(task.getExecutionId());
        taskDetail.setNodeConfig(JSON.parseObject(execution.getProcess()));//节点配置
        return taskDetail;
    }

    @Override
    public TaskDetailVo getInstDetail(Long instId) {
        //创建一个空的vo
        TaskDetailVo taskDetail = new TaskDetailVo();
        //再查询其他属性
        OaProcessInstanceHistory inst = processInstanceHistoryService.getById(instId);
        taskDetail.setFormData(JSON.parseObject(inst.getFormItems()));//表单内容
        taskDetail.setProcessInstId(instId);
        taskDetail.setInstCreator(inst.getCreator());
        taskDetail.setInstCreatorName(inst.getCreatorName());
        taskDetail.setInstCreateDate(inst.getCreateDate());
        taskDetail.setBusinessTitle(inst.getBusinessTitle());
        taskDetail.setFormName(inst.getFormName());
        OaProcessDefinition def = processDefinitionService.getById(inst.getProcessDefId());
        taskDetail.setFormItems(JSON.parseArray(def.getFormItems()));//表单配置
        return taskDetail;
    }

    @Override
    public void stopByExecutionId(Long executionId, String reason) {
        //所有未完成的任务
        List<OaTask> tasks = this.listByExecutionId(executionId, false);
        if(CollectionUtils.isEmpty(tasks)) {
            return;
        }
        tasks.forEach(e -> this.finish(OaTaskOperation.SHUTDOWN, e.getId(), null, reason, reason));
    }

    @Override
    public void stopByInstId(Long instId, String reason) {
        //所有未完成的任务
        List<OaTask> tasks = this.list(
                new LambdaQueryWrapper<OaTask>()
                    .eq(OaTask::getProcessInstId, instId)
                    .eq(OaTask::getFinished, false)
        );
        if(CollectionUtils.isEmpty(tasks)) {
            return;
        }
        tasks.forEach(e -> this.finish(OaTaskOperation.SHUTDOWN, e.getId(), null, reason, reason));
    }

    @Override
    public List<OaTask> listByInstanceId(Long instId) {
        return this.list(
                new LambdaQueryWrapper<OaTask>()
                    .eq(OaTask::getProcessInstId, instId)
        );
    }

    private List<OaTask> listByExecutionId(Long executionId, boolean finished) {
        LambdaQueryWrapper<OaTask> wrapper = new LambdaQueryWrapper<OaTask>()
                .eq(OaTask::getExecutionId, executionId)
                .eq(OaTask::getFinished, finished);
        return this.list(wrapper);
    }

    @Override
    public List<OaTask> listByActivityId(Long activityId, boolean actived) {
        QueryWrapper<OaTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaTask::getActivityId, activityId);
        queryWrapper.lambda().eq(OaTask::getActive, actived);
        queryWrapper.lambda().orderByAsc(OaTask::getCompleteSort);
        return this.list(queryWrapper);
    }
}

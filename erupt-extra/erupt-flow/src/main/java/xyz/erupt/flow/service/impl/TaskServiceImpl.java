package xyz.erupt.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
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
import xyz.erupt.flow.bean.entity.node.OaProcessNodeNobody;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeProps;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.bean.vo.TaskDetailVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.mapper.OaTaskMapper;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;
import xyz.erupt.flow.service.*;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl extends ServiceImpl<OaTaskMapper, OaTask> implements TaskService, DataProxy<OaTask> {

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
        taskOperationService.log(task, OaTaskOperation.COMPLETE, remarks);
        //触发活动的完成
        processActivityService.triggerComplete(task.getActivityId());
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
        taskUserLinkService.removeByTaskId(task.getId());
    }

    @Override
    public void refuse(Long taskId, String remarks) {
        throw new RuntimeException("未配置拒绝策略");
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
                .selectAll(OaTask.class)//查询任务全部字段
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
            //触发任务激活事件
            this.triggerTaskActive(t);
            if(OaProcessActivity.SERIAL.equals(t.getCompleteMode())) {//串行只能激活一个
                break;
            }
        }
        return oaTasks.size()>0;
    }

    /**
     * 激活任务需要处理的事情
     * 不激活任务，只做监听事件
     * @param task
     */
    @Override
    public void triggerTaskActive(OaTask task) {
        //判断任务是否有人可以处理
        if(
                task.getTaskOwner()==null//没有所属人
                && task.getAssignee()==null//没有分配人
                && taskUserLinkService.countUsersByTaskId(task.getId())<=0//候选人
        ) {//触发无人审批事件
            OaProcessExecution inst = processExecutionService.getById(task.getProcessInstId());
            OaProcessNodeProps props = inst.getProcessNode().getProps();
            OaProcessNodeNobody nobodyConf = props.getNobody();
            if(FlowConstant.NOBODY_TO_PASS.equals(nobodyConf.getHandler())) {
                //直接完成
                this.complete(task.getId(), "无人处理，自动完成");
            }else if(FlowConstant.NOBODY_TO_ADMIN.equals(nobodyConf.getHandler())) {//分配给超管用户
                LinkedHashSet<OrgTreeVo> userIds = userLinkService.getAdminUsers();
                if(CollectionUtils.isEmpty(userIds)) {
                    throw new RuntimeException("未查询到超管用户");
                }
                //将任务转办给超管
                this.assign(task.getId(), userIds, "无人处理，转办给超管用户");
            }else if(FlowConstant.NOBODY_TO_USER.equals(nobodyConf.getHandler())) {
                //将任务转办给超管
                Set<OrgTreeVo> users =
                        nobodyConf.getAssignedUser().stream().map(au -> OrgTreeVo.builder()
                                .id(au.getId())
                                .name(au.getName())
                                .build()
                        ).collect(Collectors.toSet());
                this.assign(task.getId(), users, "无人处理，转办给指定用户");
            }else if(FlowConstant.NOBODY_TO_REFUSE.equals(nobodyConf.getHandler())) {
                //直接拒绝
                this.refuse(task.getId(), "无人处理，自动拒绝");
            }else {
                throw new RuntimeException("请设置无人处理的审批方式");
            }
        }
    }

    @Override
    public void saveBatchWithUserLink(List<OaTask> oaTasks) {
        this.saveBatch(oaTasks);
        taskHistoryService.copyAndSave(oaTasks);
        oaTasks.forEach(t -> {
            t.getUserLinks().forEach(link -> link.setTaskId(t.getId()));
            taskUserLinkService.saveBatch(t.getUserLinks());
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
        OaProcessInstance inst = processInstanceService.getById(instId);
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
    public List<OaTask> listByActivityId(Long activityId, boolean actived) {
        QueryWrapper<OaTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaTask::getActivityId, activityId);
        queryWrapper.lambda().eq(OaTask::getActive, actived);
        queryWrapper.lambda().orderByAsc(OaTask::getCompleteSort);
        return this.list(queryWrapper);
    }
}

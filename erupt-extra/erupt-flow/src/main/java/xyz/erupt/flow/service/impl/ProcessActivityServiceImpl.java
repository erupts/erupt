package xyz.erupt.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.*;
import xyz.erupt.flow.bean.entity.node.*;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.mapper.OaProcessActivityMapper;
import xyz.erupt.flow.process.engine.TaskBuilder;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;
import xyz.erupt.flow.service.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessActivityServiceImpl extends ServiceImpl<OaProcessActivityMapper, OaProcessActivity>
        implements ProcessActivityService, DataProxy<OaProcessActivity> {

    @Lazy
    @Autowired
    private ProcessExecutionService processExecutionService;
    @Lazy
    @Autowired
    private ProcessActivityHistoryService processActivityHistoryService;
    @Lazy
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserLinkServiceHolder userLinkService;
    @Autowired
    private ProcessInstanceService processInstanceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(OaProcessActivity entity) {
        OaProcessActivityHistory history = new OaProcessActivityHistory();
        BeanUtils.copyProperties(entity, history);
        processActivityHistoryService.updateById(history);
        return this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int newActivities(OaProcessExecution execution, JSONObject formContent, OaProcessNode node) {
        return this.newActivities(execution, formContent, node, OaProcessExecution.STATUS_RUNNING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int newActivities(OaProcessExecution execution, JSONObject formContent, OaProcessNode node, String status) {
        if(node == null || node.getId() == null) {//如果当前节点为空
            processExecutionService.finish(execution);//更新线程的数据
            if(execution.getParentId()!=null) {
                processExecutionService.triggerActive(execution.getParentId());//使父线程激活
            }
            return 0;
        }else if(FlowConstant.NODE_TYPE_ROOT.equals(node.getType())
                || FlowConstant.NODE_TYPE_APPROVAL.equals(node.getType())
                || FlowConstant.NODE_TYPE_CC.equals(node.getType())//抄送也要创建任务
        ) {//如果是用户任务
            this.newActivity(execution, node, status, null, 1);
            return 1;
        }else if(FlowConstant.NODE_TYPE_CONDITIONS.equals(node.getType())) {//如果是互斥分支
            //主线程先继续向前，并等待
            int count = this.newActivities(
                    execution, formContent, node.getChildren(), OaProcessExecution.STATUS_WAITING);
            //根据条件选择一个分支并启动新线程
            OaProcessNode nextNode = this.switchNode(formContent, node.getBranchs());
            processExecutionService.newExecution(
                    execution.getProcessDefId(), execution.getProcessInstId(), formContent, nextNode, execution);
            return count;
        }else if(FlowConstant.NODE_TYPE_CONCURRENTS.equals(node.getType())) {//如果是并行分支
            //主线程先继续向前，并等待
            int count = this.newActivities(
                    execution, formContent, node.getChildren(), OaProcessExecution.STATUS_WAITING);
            //循环创建新线程
            for (OaProcessNode branch : node.getBranchs()) {
                processExecutionService.newExecution(
                        execution.getProcessDefId(), execution.getProcessInstId(), formContent, branch, execution);
            }
            return count;
        }else {//其他情况一律向前
            return this.newActivities(execution, formContent, node.getChildren(), status);//则继续向前
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByProcessInstId(Long procInstId) {
        QueryWrapper<OaProcessActivity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessActivity::getProcessInstId, procInstId);
        this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activeByExecutionId(Long executionId) {
        //查询下一个没激活的，进行激活
        OaProcessActivity build = this.getNexActivity(executionId, false);
        if(build==null) {
            return false;
        }
        build.setActive(true);
        processActivityHistoryService.copyAndSave(build);//同步更新历史表
        taskService.activeTaskByActivityId(build.getId());
        return true;
    }

    /**
     * 根据条件选择一个分支继续
     * @param formContent
     * @param nodes
     * @return
     */
    @Override
    public OaProcessNode switchNode(JSONObject formContent, List<OaProcessNode> nodes) {
        //按照顺序判断是否满足条件
        for (OaProcessNode node : nodes) {
            try {
                if(checkForGroups(formContent, node.getProps().getGroups(), node.getProps().getGroupsType())) {
                    return node;
                }
            }catch (Exception e) {
                log.debug("判断条件出错：" + e.getMessage());
                break;
            }
        }
        //如果都不满足，默认走第一条
        return nodes.get(0);
    }

    /**
     * 判断条件组
     * @param groups
     * @param groupsType
     * @return
     */
    private boolean checkForGroups(JSONObject form, List<OaProcessNodeGroup> groups, String groupsType) {
        if("OR".equals(groupsType)) {
            for (OaProcessNodeGroup group : groups) {
                if(checkForConditions(form, group.getConditions(), group.getGroupType())) {
                    return true;//任何一个条件满足即可
                }
            }
            return false;
        }else {//必须满足所有条件
            for (OaProcessNodeGroup group : groups) {
                if(!checkForConditions(form, group.getConditions(), group.getGroupType())) {
                    return false;//任何一个不满足就返回false
                }
            }
            return true;
        }
    }

    private boolean checkForConditions(JSONObject form, List<OaProcessNodeCondition> conditions, String groupType) {
        if("OR".equals(groupType)) {//任何一个条件满足即可
            for (OaProcessNodeCondition condition : conditions) {
                if(checkForCondition(form, condition)) {
                    return true;
                }
            }
            return false;
        }else {//必须满足所有条件
            for (OaProcessNodeCondition condition : conditions) {
                if(!checkForCondition(form, condition)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean checkForCondition(JSONObject form, OaProcessNodeCondition condition) {
        String[] value = condition.getValue();//对照值
        if(value==null || value.length<=0) {
            throw new RuntimeException("条件没有对照值");
        }
        if("Number".equals(condition.getValueType())) {//数值类型
            Double formValue = form.getDouble(condition.getId());//表单值
            if(formValue==null) {//不能报错，因为可能是测试走流程
                throw new RuntimeException("分支条件不能为空");
            }
            if("=".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))==0;
            }else if(">".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))>0;
            }else if("<".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))<0;
            }else if(">=".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))>=0;
            }else if("<=".equals(condition.getCompare())) {
                return formValue.compareTo(Double.valueOf(value[0]))<=0;
            }else if("IN".equals(condition.getCompare())) {//等于任意一个
                for (String s : value) {
                    if(formValue.compareTo(Double.valueOf(s))==0) {
                        return true;
                    }
                }
                return false;
            }else {
                if(value==null || value.length!=2) {
                    throw new RuntimeException("必须有2个对照值");
                }
                if("B".equals(condition.getCompare())) {//x < 值 < x，左右都是开区间
                    return formValue.compareTo(Double.valueOf(value[0]))>0 && formValue.compareTo(Double.valueOf(value[1]))<0;
                }else if("'AB'".equals(condition.getCompare())) {//x ≤ 值 < x，左闭右开
                    return formValue.compareTo(Double.valueOf(value[0]))>=0 && formValue.compareTo(Double.valueOf(value[1]))<0;
                }else if("'BA'".equals(condition.getCompare())) {//x < 值 ≤ x，左开右闭
                    return formValue.compareTo(Double.valueOf(value[0]))>0 && formValue.compareTo(Double.valueOf(value[1]))<=0;
                }else if("'ABA'".equals(condition.getCompare())) {//x ≤ 值 ≤ x，左右都是闭区间
                    return formValue.compareTo(Double.valueOf(value[0]))>=0 && formValue.compareTo(Double.valueOf(value[1]))<=0;
                }
            }
        }else {
            throw new RuntimeException("不支持此类条件判断"+condition.getValueType());
        }
        return false;
    }

    /**
     * 尝试完成一个节点
     * @param activityId
     */
    @Override
    public void triggerComplete(Long activityId) {
        OaProcessActivity byId = this.getById(activityId);
        //尝试激活下一个任务，如果激活成功则不能完成
        boolean a = taskService.activeTaskByActivityId(activityId);
        if(a) {
            return;
        }
        //否则删除当前节点，并修改历史表
        OaProcessActivityHistory history = new OaProcessActivityHistory();
        history.setFinished(true);
        history.setFinishDate(new Date());
        history.setId(activityId);
        processActivityHistoryService.updateById(history);
        this.removeById(activityId);
        //尝试激活下一个活动
        boolean b = this.activeByExecutionId(byId.getExecutionId());
        if(b) {//如果已经激活了，则不能继续
            return;
        }
        //然后触发线程的前进
        processExecutionService.triggerComplete(byId.getExecutionId());
    }

    /**
     * 查询下一个激活的任务
     * @param executionId
     * @return
     */
    private OaProcessActivity getNexActivity(Long executionId, boolean actived) {
        LambdaQueryWrapper<OaProcessActivity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OaProcessActivity::getExecutionId, executionId);
        queryWrapper.eq(OaProcessActivity::getActive, actived);
        queryWrapper.orderByAsc(OaProcessActivity::getSort);
        List<OaProcessActivity> list = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(list)) {
            return null;
        }else {
            return list.get(0);
        }
    }

    @Override
    public OaProcessActivity getByExecutionId(Long executionId) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("execution_id", executionId);
        queryWrapper.eq("active", true);
        return this.getOne(queryWrapper);
    }

    /**
     * 创建一个活动
     * @param execution
     * @param node
     * @param status
     * @return
     */
    private OaProcessActivity newActivity(OaProcessExecution execution, OaProcessNode node, String status, OaProcessNodeProps props, int sort) {
        OaProcessActivity build = OaProcessActivity.builder()
                .activityKey(node.getId())
                .activityName(node.getName())
                .executionId(execution.getId())
                .processInstId(execution.getProcessInstId())
                .processDefId(execution.getProcessDefId())
                .completeCondition(OaProcessActivity.COMPLETE_CONDITION_ALL)//只有全部节点完成一种
                .completeMode(OaProcessActivity.PARALLEL)//执行模式，默认并行
                .createDate(new Date())
                .active(OaProcessExecution.STATUS_RUNNING.equals(status))//如果线程状态是运行，活动才会被激活
                .finished(false)
                .finishDate(null)
                .description(node.getDesc())
                .sort(sort)
                .build();
        this.save(build);
        OaProcessActivityHistory his = new OaProcessActivityHistory();
        BeanUtils.copyProperties(build, his);
        processActivityHistoryService.save(his);//同步保存历史表
        if(props!=null) {
            this.generateTask(execution, build, node, props);
        }else {
            this.generateTask(execution, build, node, node.getProps());
        }
        processExecutionService.freshProcess(execution.getId(), JSON.toJSONString(node));//更新线程的数据
        //完成其中的开始和抄送任务
        List<OaTask> autoTasks = taskService.getTasksByActivityId(
                build.getId(), FlowConstant.NODE_TYPE_ROOT, FlowConstant.NODE_TYPE_CC
        );
        if(!CollectionUtils.isEmpty(autoTasks)) {
            autoTasks.forEach(t -> {
                taskService.complete(t.getId(), t.getTaskType() + " 任务自动完成");
            });
        }
        return build;
    }

    private void generateTask(OaProcessExecution execution, OaProcessActivity activity, OaProcessNode node, OaProcessNodeProps props) {
        TaskBuilder builder = new TaskBuilder(activity);
        //查询出当前实例
        OaProcessInstance inst = processInstanceService.getById(activity.getProcessInstId());
        //设置会签模式
        builder.setCompleteMode(props.getMode());
        builder.setTaskType(node.getType());
        builder.setActive(activity.getActive());
        /**
         * 确定用户处理人
         */
        switch (props.getAssignedType()) {
            case FlowConstant.ASSIGN_TYPE_CC://抄送人和分配人一样的处理方式
            case FlowConstant.ASSIGN_TYPE_USER://循环添加分配人
                props.getAssignedUser().forEach(au -> builder.addUser(au));
                break;
            case FlowConstant.ASSIGN_TYPE_ROLE://循环添加候选角色
                props.getRole().forEach(a -> builder.addLinkRole(a));
                break;
            case FlowConstant.ASSIGN_TYPE_SELF_SELECT://发起人自选，暂不支持
                throw new RuntimeException("暂不支持发起人自选");
            case FlowConstant.ASSIGN_TYPE_SELF:
                builder.addUser(
                        OrgTreeVo.builder()
                        .id(inst.getCreator())
                        .name("发起人")
                        .build());//将发起人作为审批人
                break;
            case FlowConstant.ASSIGN_TYPE_LEADER_TOP://发起人的所有上级
                //查询主管
                LinkedHashMap<Integer, List<OrgTreeVo>> leaderMap =
                        userLinkService.getLeaderMap(inst.getCreator(), 1, props.getLeaderTop().getLevel());
                this.forLeaders(execution, node, activity, props, leaderMap);
                return;//这种情况不需要继续后续操作
            case FlowConstant.ASSIGN_TYPE_LEADER://特定层级主管
                //查询主管
                LinkedHashMap<Integer, List<OrgTreeVo>> leaderMapNew =
                        userLinkService.getLeaderMap(inst.getCreator(), props.getLeaderTop().getLevel(), props.getLeaderTop().getLevel());
                this.forLeaders(execution, node, activity, props, leaderMapNew);
                return;//这种情况不需要继续后续操作
            case FlowConstant.ASSIGN_TYPE_FORM_USER:
                //从表单中取值
                JSONObject formContent = JSON.parseObject(inst.getFormItems());
                List<JSONObject> users = formContent.getObject(props.getFormUser(), List.class);
                if(CollectionUtils.isEmpty(users)) {
                    throw new RuntimeException("从表单中获取联系人失败");
                }
                users.forEach(u -> {
                    builder.addUser(
                            OrgTreeVo.builder()
                                .id(u.getString("id"))
                                .name(u.getString("name"))
                                .build()
                    );//全部都是分配人
                });
                break;
            default:
                throw new RuntimeException("请指定审批人");
        }
        List<OaTask> oaTasks = builder.build();
        if(CollectionUtils.isEmpty(oaTasks)) {
            throw new RuntimeException("为活动"+activity.getActivityName()+"生成任务失败");
        }
        //保存任务列表
        taskService.saveBatchWithUserLink(oaTasks);
    }

    /**
     * 解析领导审批
     * @param execution
     * @param node
     * @param props
     * @param leaderMap
     */
    private void forLeaders(
            OaProcessExecution execution, OaProcessNode node, OaProcessActivity activity
            , OaProcessNodeProps props, LinkedHashMap<Integer, List<OrgTreeVo>> leaderMap) {
        this.removeById(activity.getId());
        processActivityHistoryService.removeById(activity.getId());
        boolean first = true;
        for (Integer integer : leaderMap.keySet()) {
            List<OrgTreeVo> leaders = leaderMap.get(integer);
            OaProcessNodeProps buildProps = new OaProcessNodeProps().builder()
                    .assignedType(FlowConstant.ASSIGN_TYPE_USER)//分配给用户
                    .assignedUser(
                            leaders.stream().map(l -> {
                                OaProcessNodeAssign assign = new OaProcessNodeAssign();
                                assign.setId(l.getId());
                                return assign;
                            }).collect(Collectors.toList())
                    )
                    .mode(props.getMode())//沿用传入的会签模式
                    .nobody(props.getNobody())
                    .timeLimit(props.getTimeLimit())
                    .refuse(props.getRefuse())
                    .formPerms(props.getFormPerms())
                    .build();
            node.setDesc("第"+integer+"级主管审批");
            if(first) {//只有第一个活动激活
                this.newActivity(execution, node, OaProcessExecution.STATUS_RUNNING, buildProps, integer);
                first = false;
            }else {
                this.newActivity(execution, node, OaProcessExecution.STATUS_WAITING, buildProps, integer);
            }
        }
    }
}

package xyz.erupt.flow.process.listener.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeAssign;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeProps;
import xyz.erupt.flow.bean.vo.OrgTreeVo;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.process.builder.TaskBuilder;
import xyz.erupt.flow.process.listener.AfterCreateActivityListener;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;
import xyz.erupt.flow.service.*;
import xyz.erupt.upms.service.EruptUserService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 启动活动后生成任务
 */
@Component
public class NewTaskListener implements AfterCreateActivityListener {

    @Override
    public int sort() {
        return 0;
    }

    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private ProcessExecutionService processExecutionService;
    @Autowired
    private ProcessActivityService processActivityService;
    @Autowired
    private ProcessActivityHistoryService processActivityHistoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserLinkServiceHolder userLinkService;
    @Autowired
    private EruptUserService eruptUserService;

    @Override
    public void execute(OaProcessActivity activity) {
        OaProcessExecution execution = processExecutionService.getById(activity.getExecutionId());
        OaProcessNode node = activity.getProcessNode();

        //生成任务
        this.generateTask(execution, activity, node, node.getProps());
    }

    private void generateTask(OaProcessExecution execution, OaProcessActivity activity, OaProcessNode node, OaProcessNodeProps props) {
        TaskBuilder builder = new TaskBuilder(activity);
        //查询出当前实例
        OaProcessInstance inst = processInstanceService.getById(activity.getProcessInstId());
        //设置会签模式
        builder.setCompleteMode(props.getMode());
        builder.setTaskType(node.getType());
        builder.setActive(activity.getActive());
        //如果是开始节点，直接指定处理人
        if(FlowConstant.NODE_TYPE_ROOT_VALUE.equals(activity.getActivityKey())) {
            builder.addUser(OrgTreeVo.builder()
                    .id(inst.getCreator())
                    .build());
        }else {
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
                    int endLevel = props.getLeaderTop().getLevel();//最多x级的领导
                    if("TOP".equals(props.getLeaderTop().getEndCondition())) {
                        endLevel = -1;//不限制层级
                    }
                    //查询主管
                    LinkedHashMap<Integer, List<OrgTreeVo>> leaderMap =
                            userLinkService.getLeaderMap(inst.getCreator(), 1, endLevel);
                    this.forLeaders(execution, node, activity, props, leaderMap);
                    return;//这种情况不需要继续后续操作
                case FlowConstant.ASSIGN_TYPE_LEADER://特定层级主管
                    //查询主管
                    LinkedHashMap<Integer, List<OrgTreeVo>> leaderMapNew =
                            userLinkService.getLeaderMap(inst.getCreator(), props.getLeader().getLevel(), props.getLeader().getLevel());
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
        //这种情况要删除原本的活动
        processActivityService.removeById(activity.getId());
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

            if(first) {//只有第一个活动激活，生成新活动，注意这个活动不能触发后置监听器，不然胡递归
                processActivityService.newActivity(execution, node, OaProcessExecution.STATUS_RUNNING, buildProps, integer);
                first = false;
            }else {
                processActivityService.newActivity(execution, node, OaProcessExecution.STATUS_WAITING, buildProps, integer);
            }
        }
    }
}

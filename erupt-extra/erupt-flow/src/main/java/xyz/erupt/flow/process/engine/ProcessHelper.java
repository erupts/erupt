package xyz.erupt.flow.process.engine;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.flow.bean.entity.OaProcessActivityHistory;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.node.*;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.process.engine.condition.*;
import xyz.erupt.flow.service.*;

import java.text.ParseException;
import java.util.*;

@Component
@Slf4j
public class ProcessHelper {

    @Lazy
    @Autowired
    private ProcessActivityHistoryService processActivityHistoryService;
    @Lazy
    @Autowired
    private ProcessInstanceService processInstanceService;
    @Lazy
    @Autowired
    private ProcessActivityService processActivityService;
    @Lazy
    @Autowired
    private ProcessExecutionService processExecutionService;
    @Lazy
    @Autowired
    private TaskService taskService;

    private Map<String, ConditionChecker> checkerMap = new HashMap<>();

    @Autowired
    public ProcessHelper(ConditionChecker... checkers) {
        //将所有的检查者编成map
        for (ConditionChecker checker : checkers) {
            if(checker instanceof NumberChecker) {
                this.checkerMap.put("Number", checker);
            }else if(checker instanceof DateChecker) {
                this.checkerMap.put("Date", checker);
            }else if(checker instanceof StringChecker) {
                this.checkerMap.put("String", checker);
            }else if(checker instanceof UserChecker) {
                this.checkerMap.put("User", checker);
            }else if(checker instanceof DeptChecker) {
                this.checkerMap.put("Dept", checker);
            }
        }
    }

    /**
     * 跳转到指定活动
     * 跳转后本流程会到达目标活动，并且只激活它
     * 其他目前激活的活动会终止
     * 并且要保证 唯一激活的这个活动，后续能够正常完成
     * 实现方案分2种
     * 如果是同一线程内：直接跳转就可以了
     * 如果是不同线程：需要从开始节点开始正向模拟，除了目标节点外，其他活动全部自动完成
     * @param source
     * @param target
     */
    public void jumpTo(OaTask task, String source, String target) {
        if(source.equals(target)) {
            throw new EruptApiErrorTip("禁止跳转到当前节点");
        }
        //跳转之前，要先确定是本线程跳转还是跨线程跳转
        OaProcessInstance inst = processInstanceService.getById(task.getProcessInstId());
        boolean inOneExecution = this.isSameExecution(inst.getProcessNode(), Arrays.asList(new String[]{source, target}), Arrays.asList(new String[]{source, target}));
        //本线程内的跳转，只需要将本线程内的所有活动全部终止
        if(inOneExecution) {
            //这两个强行删除，不触发事件
            processActivityService.stopByExecutionId(task.getExecutionId(), "节点跳转");
            taskService.stopByExecutionId(task.getExecutionId(), "节点跳转");
            OaProcessExecution execution = processExecutionService.getById(task.getExecutionId());
            //当前线程下，继续进行
            OaProcessNode nextNode = this.findByKey(inst.getProcessNode(), target);
            processActivityService.newActivities(execution, JSON.parseObject(inst.getFormItems()), nextNode);
        }
        //跨线程跳转，要将本实例所有线程全部终止
        else {
            throw new EruptApiErrorTip("暂不支持跨线程跳转");
        }
    }

    /**
     * 先判断是否同一线程
     * @param processNode
     * @param allKeys 完整的key
     * @param noFoundKeys 等待判断的key，这两个数组都不能为空
     * @return
     */
    private boolean isSameExecution(OaProcessNode processNode, List<String> allKeys, List<String> noFoundKeys) {
        if(processNode==null || processNode.getId()==null) {
            return false;
        }
        List<String> newNoFoundKeys = new ArrayList<>();
        for (String key : noFoundKeys) {
            if(!processNode.getId().equals(key)) {
                newNoFoundKeys.add(key);//没有命中的，保留下来
            }
        }
        if(newNoFoundKeys.size()<=0) {//表示全部命中了，返回true
            return true;
        }else {//否则赋值
            noFoundKeys = newNoFoundKeys;
        }
        //然后继续判断
        if(!CollectionUtils.isEmpty(processNode.getBranchs())) {//有分支，则分支内要包含有全部的节点
            for (OaProcessNode branch : processNode.getBranchs()) {
                if(this.isSameExecution(branch, allKeys, allKeys)) {
                    return true;
                }
            }
            //如果分支内没找到，继续向前，附带全部key
            return this.isSameExecution(processNode.getChildren(), allKeys, allKeys);
        }else {//其他情况继续向前
            return this.isSameExecution(processNode.getChildren(), allKeys, noFoundKeys);
        }
    }

    private OaProcessNode findByKey(OaProcessNode processNode, String target) {
        if(processNode==null || processNode.getId()==null) {
            return null;
        }
        if(target.equals(processNode.getId())) {
            return processNode;
        }
        //先遍历分支
        if(processNode.getBranchs()!=null) {
            for (OaProcessNode branch : processNode.getBranchs()) {
                OaProcessNode tmpNode = this.findByKey(branch, target);
                if(tmpNode!=null) {
                    return tmpNode;
                }
            }
        }
        //再向前
        return this.findByKey(processNode.getChildren(), target);
    }

    /**
     * 获取流程的上一个用户任务
     * 只能从流程图上获取，而不能按照实际执行获取
     * @param activityKey
     * @return
     */
    public void getPreUserTasks(OaProcessNode currentNode, OaProcessNode lastUserTask, String activityKey, Set<OaProcessNode> preNodes) {
        if(FlowConstant.NODE_TYPE_ROOT.equals(currentNode.getType())
                || FlowConstant.NODE_TYPE_APPROVAL.equals(currentNode.getType())
        ) {//这几种情况要刷新最后的用户任务
            lastUserTask = currentNode;
        }

        List<OaProcessNode> branchs = currentNode.getBranchs();
        if(!CollectionUtils.isEmpty(branchs)) {//如果有分支，要先进分支
            for (OaProcessNode branch : branchs) {//否则遍历
                this.getPreUserTasks(branch, lastUserTask, activityKey, preNodes);
            }
        }else {
            if(currentNode.getChildren()==null) {//没有子节点，就到头了
                return;
            }else {//有子节点就继续
                //命中就返回
                if(activityKey.equals(currentNode.getChildren().getId())) {
                    preNodes.add(lastUserTask);
                }else {//不命中，继续向下
                    this.getPreUserTasks(currentNode.getChildren(), lastUserTask, activityKey, preNodes);
                }
            }
        }
    }

    /**
     * 根据条件选择一个分支继续
     * @param formContent
     * @param nodes
     * @return
     */
    public OaProcessNode switchNode(OaProcessExecution execution, JSONObject formContent, List<OaProcessNode> nodes) {
        //按照顺序判断是否满足条件
        OaProcessNode defaultNode = null;
        for (OaProcessNode node : nodes) {
            try {
                if(node.getProps().isDefault()) {//默认条件无需判断
                    if(defaultNode==null) {
                        defaultNode = node;
                    }
                }else if(checkForGroups(execution, formContent, node.getProps().getGroups(), node.getProps().getGroupsType())) {
                    return node;
                }
            }catch (Exception e) {
                e.printStackTrace();
                log.debug("判断条件出错：" + e.getMessage());
                //break;
            }
        }
        //如果都不满足，走第一个默认条件
        if(defaultNode==null) {
            throw new EruptApiErrorTip("没有符合的条件，请联系管理员");
        }
        return defaultNode;
    }

    /**
     * 判断条件组
     * @param groups
     * @param groupsType
     * @return
     */
    private boolean checkForGroups(OaProcessExecution execution, JSONObject form, List<OaProcessNodeGroup> groups, String groupsType) {
        if("OR".equals(groupsType)) {
            for (OaProcessNodeGroup group : groups) {
                if(checkForConditions(execution, form, group.getConditions(), group.getGroupType())) {
                    return true;//任何一个条件满足即可
                }
            }
            return false;
        }else {//必须满足所有条件
            for (OaProcessNodeGroup group : groups) {
                if(!checkForConditions(execution, form, group.getConditions(), group.getGroupType())) {
                    return false;//任何一个不满足就返回false
                }
            }
            return true;
        }
    }

    private boolean checkForConditions(OaProcessExecution execution, JSONObject form, List<OaProcessNodeCondition> conditions, String groupType) {
        if("OR".equals(groupType)) {//任何一个条件满足即可
            for (OaProcessNodeCondition condition : conditions) {
                if(checkForCondition(execution, form, condition)) {
                    return true;
                }
            }
            return false;
        }else {//必须满足所有条件
            for (OaProcessNodeCondition condition : conditions) {
                if(!checkForCondition(execution, form, condition)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean checkForCondition(OaProcessExecution execution, JSONObject form, OaProcessNodeCondition condition) {
        ConditionChecker conditionChecker = this.checkerMap.get(condition.getValueType());
        if(conditionChecker==null) {//数值类型
            throw new RuntimeException("不支持此类条件判断"+condition.getValueType());
        }
        try {
            return conditionChecker.check(execution, form, condition);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 审批拒绝
     */
    public void refuse(OaTask task, String accountName) {
        //取得拒绝策略
        OaProcessActivityHistory activityHistory = processActivityHistoryService.getById(task.getActivityId());
        OaProcessNode processNode = activityHistory.getProcessNode();
        OaProcessNodeProps props = processNode.getProps();
        if(props==null) {
            throw new EruptApiErrorTip("请先配置拒绝策略");
        }
        OaProcessNodeRefuse refuse = props.getRefuse();
        if(refuse==null) {
            throw new EruptApiErrorTip("请先配置拒绝策略");
        }
        if(FlowConstant.REFUSE_TO_END.equals(refuse.getType())) {//流程的终止
            processInstanceService.stop(activityHistory.getProcessInstId(), accountName+" 审批拒绝");
        }else if(FlowConstant.REFUSE_TO_BEFORE.equals(refuse.getType())) {//回到上一步
            //获取本线程的上一步
            OaProcessInstance inst = processInstanceService.getById(task.getProcessInstId());
            Set<OaProcessNode> preNodes = new HashSet<>();
            this.getPreUserTasks(inst.getProcessNode(), null, activityHistory.getActivityKey(), preNodes);
            if(preNodes==null || preNodes.size()<=0) {
                throw new EruptApiErrorTip("流程没有上一步");
            }else if(preNodes.size()>1) {
                throw new EruptApiErrorTip("流程的前置节点不唯一，无法回退");
            }
            //将本流程实例跳转到指定步骤
            this.jumpTo(task, task.getActivityKey(), preNodes.stream().findAny().get().getId());
        }else if(FlowConstant.REFUSE_TO_NODE.equals(refuse.getType())) {
            this.jumpTo(task, task.getActivityKey(), refuse.getTarget());
        }else {
            throw new EruptApiErrorTip("无法识别拒绝策略"+refuse.getType());
        }
    }
}

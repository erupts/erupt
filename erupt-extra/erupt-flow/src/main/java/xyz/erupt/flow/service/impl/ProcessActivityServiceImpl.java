package xyz.erupt.flow.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.*;
import xyz.erupt.flow.mapper.OaProcessActivityMapper;
import xyz.erupt.flow.service.ProcessActivityHistoryService;
import xyz.erupt.flow.service.ProcessActivityService;
import xyz.erupt.flow.service.ProcessExecutionService;
import xyz.erupt.flow.service.TaskService;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
        }else if("ROOT".equals(node.getType()) || "APPROVAL".equals(node.getType())) {//如果是用户任务
            this.newActivity(execution, node, status);
            return 1;
        }else if("CONDITIONS".equals(node.getType())) {//如果是互斥分支
            //主线程先继续向前，并等待
            int count = this.newActivities(
                    execution, formContent, node.getChildren(), OaProcessExecution.STATUS_WAITING);
            //根据条件选择一个分支并启动新线程
            OaProcessNode nextNode = this.switchNode(formContent, node.getBranchs());
            processExecutionService.newExecution(
                    execution.getProcessDefId(), execution.getProcessInstId(), formContent, nextNode, execution);
            return count;
        }else if("CONCURRENTS".equals(node.getType())) {//如果是并行分支
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
    public void removeByProcessInstId(Serializable procInstId) {
        QueryWrapper<OaProcessActivity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessActivity::getProcessInstId, procInstId);
        this.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activeByExecutionId(Long executionId) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("execution_id", executionId);
        this.update(
                OaProcessActivity.builder()
                    .active(true)
                    .build()
                , queryWrapper
        );
        OaProcessActivityHistory build = OaProcessActivityHistory.builder()
                .build();
        build.setActive(true);
        processActivityHistoryService.update(build, queryWrapper);//同步更新历史表
        taskService.activeTaskByExecutionId(executionId);
    }

    private OaProcessActivity newActivity(OaProcessExecution execution, OaProcessNode node, String status) {
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
                .build();
        this.save(build);
        OaProcessActivityHistory his = new OaProcessActivityHistory();
        BeanUtils.copyProperties(build, his);
        processActivityHistoryService.save(his);//同步保存历史表
        taskService.generateTask(build);

        processExecutionService.freshProcess(execution.getId(), JSON.toJSONString(node));//更新线程的数据
        return build;
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
            if(checkForGroups(formContent, node.getProps().getGroups(), node.getProps().getGroupsType())) {
                return node;
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
            if(formValue==null) {
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
        List<OaTask> oaTasks = taskService.listByActivityId(activityId);
        if(CollectionUtil.isNotEmpty(oaTasks)) {//如果还有任务需要执行，就不能完成此节点
            if(OaProcessActivity.SERIAL.equals(byId.getCompleteMode())) {//如果是串行模式，将下一个任务激活
                taskService.activeTask(oaTasks.get(0).getId());
            }
            return;
        }
        //否则删除当前节点，并修改历史表
        OaProcessActivityHistory history = new OaProcessActivityHistory();
        history.setFinished(true);
        history.setFinishDate(new Date());
        history.setId(activityId);
        processActivityHistoryService.updateById(history);
        this.removeById(activityId);
        //然后触发线程的前进
        processExecutionService.triggerComplete(byId.getExecutionId());
    }

    @Override
    public OaProcessActivity getByExecutionId(Long executionId) {
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("execution_id", executionId);
        queryWrapper.eq("active", true);
        return this.getOne(queryWrapper);
    }
}

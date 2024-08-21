package xyz.erupt.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessActivityHistory;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;
import xyz.erupt.flow.bean.entity.node.OaProcessNodeProps;
import xyz.erupt.flow.constant.FlowConstant;
import xyz.erupt.flow.mapper.OaProcessActivityMapper;
import xyz.erupt.flow.process.engine.ProcessHelper;
import xyz.erupt.flow.process.listener.AfterActiveActivityListener;
import xyz.erupt.flow.process.listener.AfterCreateActivityListener;
import xyz.erupt.flow.process.listener.AfterFinishActivityListener;
import xyz.erupt.flow.process.listener.ExecutableNodeListener;
import xyz.erupt.flow.process.userlink.impl.UserLinkServiceHolder;
import xyz.erupt.flow.service.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Data
public class ProcessActivityServiceImpl extends ServiceImpl<OaProcessActivityMapper, OaProcessActivity>
        implements ProcessActivityService, DataProxy<OaProcessActivity> {

    private Map<Class<ExecutableNodeListener>, List<ExecutableNodeListener>> listenerMap = new HashMap<>();

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
    @Autowired
    private ProcessHelper processHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int newActivitiesForExecution(OaProcessExecution execution) {
        OaProcessInstance inst = processInstanceService.getById(execution.getProcessInstId());
        return this.newActivities(execution, inst.getFormContent(), execution.getProcessNode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int newActivities(OaProcessExecution execution, JSONObject formContent, OaProcessNode node) {
        return this.newActivities(execution, formContent, node, OaProcessExecution.STATUS_RUNNING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int newActivities(OaProcessExecution execution, JSONObject formContent, OaProcessNode node, String status) {
        if(node == null || node.getId() == null) {//如果当前节点为空，表示当前线程已结束
            if(OaProcessExecution.STATUS_RUNNING.equals(status)) {
                processExecutionService.finish(execution);//调用线程结束方法
            }
            return 0;
        }
        if(FlowConstant.NODE_TYPE_ROOT.equals(node.getType())
                || FlowConstant.NODE_TYPE_APPROVAL.equals(node.getType())
                || FlowConstant.NODE_TYPE_CC.equals(node.getType())//抄送也要创建任务
        ) {//这几种情况需要创建活动
            this.newActivity(execution, node, status, null, 1);
            return 1;
        }
        //其他情况本节点都不会生成活动，而是会继续向前
        if(FlowConstant.NODE_TYPE_CONDITIONS.equals(node.getType())) {//如果是互斥分支
            //主线程先继续向前，并等待
            int count = this.newActivities(
                    execution, formContent, node.getChildren(), OaProcessExecution.STATUS_WAITING);
            //根据条件选择一个分支并启动新线程
            OaProcessNode nextNode = processHelper.switchNode(execution, formContent, node.getBranchs());
            processExecutionService.newExecution(
                    execution.getProcessDefId(), execution.getProcessInstId(), nextNode, execution);
            return count;
        }else if(FlowConstant.NODE_TYPE_CONCURRENTS.equals(node.getType())) {//如果是并行分支
            //主线程先继续向前，并等待
            int count = this.newActivities(
                    execution, formContent, node.getChildren(), OaProcessExecution.STATUS_WAITING);
            //循环创建新线程
            for (OaProcessNode branch : node.getBranchs()) {
                processExecutionService.newExecution(
                        execution.getProcessDefId(), execution.getProcessInstId(), branch, execution);
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
        this.updateById(build);

        this.listenerMap.get(AfterActiveActivityListener.class).forEach(l -> l.execute(build));
        return true;
    }

    @Override
    public void stopByExecutionId(Long executionId, String reason) {
        List<OaProcessActivity> activities = this.listByExecutionId(executionId, true);
        if(CollectionUtils.isEmpty(activities)) {
            return;
        }
        activities.forEach(e -> {
            OaProcessActivity build = OaProcessActivity.builder()
                    .active(false)
                    .id(e.getId())
                    .build();
            this.updateById(build);
            processActivityHistoryService.copyAndSave(build);
        });
    }

    @Override
    public void stopByInstId(Long instId, String reason) {
        List<OaProcessActivity> activities = this.list(
                new LambdaQueryWrapper<OaProcessActivity>()
                    .eq(OaProcessActivity::getProcessInstId, instId)
                    .eq(OaProcessActivity::getFinished, false)
        );
        if(CollectionUtils.isEmpty(activities)) {
            return;
        }
        activities.forEach(e -> {
            OaProcessActivity build = OaProcessActivity.builder()
                    .active(false)
                    .id(e.getId())
                    .build();
            this.updateById(build);
            processActivityHistoryService.copyAndSave(build);
        });
    }

    private List<OaProcessActivity> listByExecutionId(Long executionId, boolean active) {
        LambdaQueryWrapper<OaProcessActivity> wrapper = new LambdaQueryWrapper<OaProcessActivity>()
                .eq(OaProcessActivity::getExecutionId, executionId);
        return this.list(wrapper);
    }

    /**
     * 尝试完成一个节点
     * @param activityId
     */
    @Override
    public void complete(Long activityId) {
        OaProcessActivity activity = this.getById(activityId);
        //删除当前节点，并修改历史表
        OaProcessActivityHistory history = new OaProcessActivityHistory();
        history.setFinished(true);
        history.setFinishDate(new Date());
        history.setId(activityId);
        processActivityHistoryService.updateById(history);
        this.removeById(activityId);
        //触发后置监听
        this.listenerMap.get(AfterFinishActivityListener.class).forEach(l -> l.execute(activity));
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
    @Override
    public OaProcessActivity newActivity(OaProcessExecution execution, OaProcessNode node, String status, OaProcessNodeProps props, int sort) {
        node.setProps(props==null? node.getProps(): props);//配置继承过来
        OaProcessActivity activity = OaProcessActivity.builder()
                .activityKey(node.getId())
                .activityName(node.getName())
                .node(JSON.toJSONString(node))
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
        this.save(activity);
        processActivityHistoryService.copyAndSave(activity);//同步保存历史表

        //触发创建后置监听
        this.listenerMap.get(AfterCreateActivityListener.class).forEach(l -> l.execute(activity));

        return activity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(OaProcessActivity entity) {
        OaProcessActivityHistory history = new OaProcessActivityHistory();
        BeanUtils.copyProperties(entity, history);
        processActivityHistoryService.updateById(history);
        return super.updateById(entity);
    }
}

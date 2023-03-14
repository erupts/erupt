package xyz.erupt.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;
import xyz.erupt.flow.mapper.OaProcessExecutionMapper;
import xyz.erupt.flow.service.ProcessActivityService;
import xyz.erupt.flow.service.ProcessExecutionService;
import xyz.erupt.flow.service.ProcessInstanceService;

import java.io.Serializable;
import java.util.Date;

@Service
public class ProcessExecutionServiceImpl extends ServiceImpl<OaProcessExecutionMapper, OaProcessExecution>
        implements ProcessExecutionService {

    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private ProcessActivityService processActivityService;

    @Override
    public OaProcessExecution newExecution(String defId, Long instanceId, JSONObject formContent, OaProcessNode startNode, OaProcessExecution parent) {
        //创建线程
        OaProcessExecution execution = OaProcessExecution.builder()
                .processInstId(instanceId)
                .processDefId(defId)
                .startNodeId(startNode.getId())
                .startNodeName(startNode.getName())
                .status(OaProcessExecution.STATUS_RUNNING)
                .created(new Date())
                .build();
        if(parent==null) {
            execution.setId(instanceId);//主线程id与流程实例相同
            execution.setParentId(null);
        }else{
            execution.setId(null);
            execution.setParentId(parent.getId());
        }
        super.save(execution);
        processActivityService.newActivities(execution, formContent, startNode, OaProcessExecution.STATUS_RUNNING);
        return execution;
    }

    private int countRunningChildren(Long parentId) {
        QueryWrapper<OaProcessExecution> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessExecution::getParentId, parentId);
        //运行中和等待中的任务都算
        queryWrapper.lambda().in(OaProcessExecution::getStatus
                , OaProcessExecution.STATUS_RUNNING, OaProcessExecution.STATUS_WAITING);
        return this.count(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByProcessInstId(Long procInstId) {
        QueryWrapper<OaProcessExecution> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(OaProcessExecution::getProcessInstId, procInstId);
        this.remove(queryWrapper);
    }

    /**
     * 当有任务完成时触发此方法
     * 判断当前线程是否还有任务需要完成，如果没有，则进行下一步
     * 暂时只支持一种完成方式，即线程内的所有任务全部完成
     * @param executionId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerComplete(Long executionId) {
        //如果当前线程还有激活的活动，不会继续
        OaProcessActivity activity = processActivityService.getByExecutionId(executionId);
        if(activity!=null) {//这种情况要报错，因为一个线程同时有2个活动是异常的
            throw new RuntimeException("当前线程还有活动未完成");
        }
        //否则判断是否满足继续的条件
        int count = this.countRunningChildren(executionId);
        if(count>0) {
            return;
        }
        OaProcessExecution execution = this.getById(executionId);
        if(!OaProcessExecution.STATUS_RUNNING.equals(execution.getStatus())) {
            throw new EruptApiErrorTip("当前线程状态"+execution.getStatus()+"不可完成");
        }
        //判断当前线程是否结束
        boolean executionFinished = false;
        OaProcessNode nextNode = null;
        if(execution.getProcess()==null) {
            executionFinished = true;
        }else {
            nextNode = JSON.parseObject(execution.getProcess(), OaProcessNode.class).getChildren();
            if(nextNode==null || nextNode.getId()==null) {
                executionFinished = true;
            }
        }

        if(executionFinished) {//当前线程结束了
            this.finish(execution);
            //尝试完成父线程
            if(execution.getParentId()!=null) {
                this.triggerComplete(execution.getParentId());
            }else {//直接完成流程实例
                processInstanceService.finish(execution.getProcessInstId());
            }
            return;
        }else {//当前线程没结束
            //生成下一批任务
            OaProcessInstance inst = processInstanceService.getById(execution.getProcessInstId());
            JSONObject jsonObject = JSON.parseObject(inst.getFormItems());
            processActivityService.newActivities(execution, jsonObject, nextNode, OaProcessExecution.STATUS_RUNNING);
        }
    }

    //完成一个线程
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(OaProcessExecution execution) {
        Date now = new Date();
        execution.setProcess("{}");
        execution.setStatus(OaProcessExecution.STATUS_ENDED);
        execution.setEnded(now);
        execution.setUpdated(new Date());
        this.updateById(execution);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void freshProcess(Long id, String json) {
        OaProcessExecution build = OaProcessExecution.builder()
                .process(json)
                .updated(new Date())
                .build();
        build.setId(id);
        this.updateById(build);
    }

    /**
     * 继续线程
     * @param executionId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerActive(Long executionId) {
        //否则判断是否满足继续的条件
        int count = this.countRunningChildren(executionId);
        if(count>0) {
            //当前线程还有子线程未合并，不能继续
            return;
        }
        //否则激活
        OaProcessExecution updateBean = OaProcessExecution.builder()
                .status(OaProcessExecution.STATUS_RUNNING)
                .build();
        updateBean.setId(executionId);
        this.updateById(updateBean);
        //激活线程下的所有活动和任务
        processActivityService.activeByExecutionId(executionId);
    }

}

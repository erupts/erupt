package xyz.erupt.flow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.flow.bean.entity.OaProcessDefinition;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.bean.entity.node.OaProcessNode;
import xyz.erupt.flow.process.listener.AfterCreateExecutionListener;
import xyz.erupt.flow.process.listener.AfterFinishExecutionListener;
import xyz.erupt.flow.process.listener.ExecutableNodeListener;
import xyz.erupt.flow.service.ProcessActivityService;
import xyz.erupt.flow.service.ProcessDefinitionService;
import xyz.erupt.flow.service.ProcessExecutionService;
import xyz.erupt.flow.service.ProcessInstanceService;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.jpa.dao.EruptLambdaQuery;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Service
public class ProcessExecutionServiceImpl
        implements ProcessExecutionService {

    private Map<Class<ExecutableNodeListener>, List<ExecutableNodeListener>> listenerMap = new HashMap<>();

    @Autowired
    private ProcessInstanceService processInstanceService;
    @Autowired
    private ProcessActivityService processActivityService;
    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Resource
    private EruptDao eruptDao;

    @Override
    @Transactional
    public OaProcessExecution newExecutionForInstance(OaProcessInstance inst) {
        OaProcessDefinition def = processDefinitionService.getById(inst.getProcessDefId());
        return this.newExecution(def.getId(), inst.getId(), def.getProcessNode(), null);
    }

    @Override
    public OaProcessExecution getById(Long id) {
        return eruptDao.findById(OaProcessExecution.class, id);
    }

    @Override
    @Transactional
    public OaProcessExecution newExecution(String defId, Long instanceId, OaProcessNode startNode, OaProcessExecution parent) {
        //创建线程
        OaProcessExecution execution = OaProcessExecution.builder()
                .processInstId(instanceId)
                .processDefId(defId)
                .startNodeId(startNode.getId())
                .startNodeName(startNode.getName())
                .process(JSON.toJSONString(startNode))
                .status(OaProcessExecution.STATUS_RUNNING)//直接就是运行状态
                .created(new Date())
                .build();
        if (parent == null) {
            execution.setId(instanceId);//主线程id与流程实例相同
            execution.setParentId(null);
        } else {
            execution.setId(null);
            execution.setParentId(parent.getId());
        }
        eruptDao.persist(execution);
        //创建后置监听
        this.listenerMap.get(AfterCreateExecutionListener.class).forEach(l -> l.execute(execution));

        return execution;
    }

    /**
     * 线程前进
     *
     * @param executionId
     */
    @Override
    @Transactional
    public void step(Long executionId, OaProcessNode currentNode) {
        //判断是否满足继续的条件
        Long count = this.countRunningChildren(executionId);
        if (count > 0) {
            //如果还有运行中的子线程，禁止前进
            return;
        }
        OaProcessExecution execution = eruptDao.findById(OaProcessExecution.class, executionId);
        if (!OaProcessExecution.STATUS_RUNNING.equals(execution.getStatus())) {
            throw new EruptApiErrorTip("当前线程状态" + execution.getStatus() + "不可完成");
        }

        //获取下一个节点
        OaProcessNode nextNode = null;
        if (execution.getProcess() != null) {
            nextNode = currentNode.getChildren();
        }

        if (nextNode == null || nextNode.getId() == null) {//没有下一步，当前线程结束了
            this.finish(execution);//调用结束
        } else {//当前线程没结束，生成下一批活动
            OaProcessInstance inst = eruptDao.findById(OaProcessInstance.class, execution.getProcessInstId());
            JSONObject jsonObject = JSON.parseObject(inst.getFormItems());
            processActivityService.newActivities(execution, jsonObject, nextNode, OaProcessExecution.STATUS_RUNNING);
        }
    }

    private Long countRunningChildren(Long parentId) {
        return eruptDao.lambdaQuery(OaProcessExecution.class).eq(OaProcessExecution::getParentId, parentId).in(OaProcessExecution::getStatus
                , OaProcessExecution.STATUS_RUNNING, OaProcessExecution.STATUS_WAITING).count();
    }

    @Override
    @Transactional
    public void removeByProcessInstId(Long procInstId) {
        for (OaProcessExecution execution : eruptDao.lambdaQuery(OaProcessExecution.class).eq(OaProcessExecution::getProcessInstId, procInstId).list()) {
            eruptDao.delete(execution);
        }
    }

    //完成一个线程
    @Override
    @Transactional
    public void finish(OaProcessExecution execution) {
        //线程结束
        Date now = new Date();
        execution.setProcess("{}");
        execution.setStatus(OaProcessExecution.STATUS_ENDED);
        execution.setEnded(now);
        execution.setUpdated(new Date());
        eruptDao.merge(execution);
        //触发结束后置监听
        this.listenerMap.get(AfterFinishExecutionListener.class).forEach(it -> it.execute(execution));
    }

    @Override
    @Transactional
    public void freshProcess(Long id, String json) {
        OaProcessExecution build = OaProcessExecution.builder()
                .process(json)
                .updated(new Date())
                .build();
        build.setId(id);
        eruptDao.persist(build);
    }

    @Override
    public void stopByInstId(Long instId, String reason) {
        Date now = new Date();
        List<OaProcessExecution> executions = this.listByInstId(instId, true);
        if (CollectionUtils.isEmpty(executions)) {
            return;
        }
        executions.forEach(e -> {
            OaProcessExecution build = OaProcessExecution.builder()
                    .process("{}")
                    .status(OaProcessExecution.STATUS_ENDED)
                    .reason(reason)
                    .ended(now)
                    .updated(now)
                    .build();
            build.setId(e.getId());
            eruptDao.merge(build);
        });
    }

    private List<OaProcessExecution> listByInstId(Long instId, boolean active) {
        EruptLambdaQuery<OaProcessExecution> processExecutionEruptLambdaQuery = eruptDao.lambdaQuery(OaProcessExecution.class).eq(OaProcessExecution::getProcessInstId, instId);
        if (active) {
            processExecutionEruptLambdaQuery.in(OaProcessExecution::getStatus, OaProcessExecution.STATUS_RUNNING, OaProcessExecution.STATUS_WAITING);
        } else {
            processExecutionEruptLambdaQuery.in(OaProcessExecution::getStatus, OaProcessExecution.STATUS_ENDED);
        }
        return processExecutionEruptLambdaQuery.list();
    }

    /**
     * 继续线程
     *
     * @param executionId
     */
    @Override
    @Transactional
    public void active(Long executionId) {
        //否则判断是否满足继续的条件
        Long count = this.countRunningChildren(executionId);
        if (count > 0) {
            //当前线程还有子线程未合并，不能继续
            return;
        }
        //否则激活
        OaProcessExecution updateBean = OaProcessExecution.builder()
                .status(OaProcessExecution.STATUS_RUNNING)
                .build();
        updateBean.setId(executionId);
        eruptDao.merge(updateBean);
        //激活线程下的所有活动和任务
        processActivityService.activeByExecutionId(executionId);
    }

}

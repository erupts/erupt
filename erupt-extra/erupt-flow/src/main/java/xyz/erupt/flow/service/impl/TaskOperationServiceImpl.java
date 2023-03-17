package xyz.erupt.flow.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskOperation;
import xyz.erupt.flow.repository.OaTaskOperationRepository;
import xyz.erupt.flow.service.TaskOperationService;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Date;
import java.util.List;

@Service
public class TaskOperationServiceImpl implements TaskOperationService, DataProxy<OaTaskOperation> {

    @Autowired
    private EruptUserService eruptUserService;
    @Autowired
    private OaTaskOperationRepository taskOperationRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void log(OaTask task, String operation, String remarks) {
        EruptUser currentEruptUser = eruptUserService.getCurrentEruptUser();
        OaTaskOperation build = OaTaskOperation.builder()
                .processInstId(task.getProcessInstId())
                .processDefId(task.getProcessDefId())
                .taskId(task.getId())
                .taskName(task.getTaskName())
                .operator(currentEruptUser.getAccount())
                .operatorName(currentEruptUser.getName())
                .operation(operation)
                .remarks(remarks)
                .operationDate(new Date())
                .build();
        taskOperationRepository.save(build);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void log(OaTask task, String operation, String remarks, String nodeId, String nodeName) {
        EruptUser currentEruptUser = eruptUserService.getCurrentEruptUser();
        OaTaskOperation build = OaTaskOperation.builder()
                .processInstId(task.getProcessInstId())
                .processDefId(task.getProcessDefId())
                .taskId(task.getId())
                .taskName(task.getTaskName())
                .operator(currentEruptUser.getAccount())
                .operatorName(currentEruptUser.getName())
                .operation(operation)
                .remarks(remarks)
                .operationDate(new Date())
                .targetNodeId(nodeId)
                .targetNodeName(nodeName)
                .build();
        taskOperationRepository.save(build);
    }

    @Override
    public List<OaTaskOperation> listByOperator(String account) {
        List<OaTaskOperation> list = taskOperationRepository.findAllByOperatorOrderByOperationDateDesc(account);
        return list;
    }
}

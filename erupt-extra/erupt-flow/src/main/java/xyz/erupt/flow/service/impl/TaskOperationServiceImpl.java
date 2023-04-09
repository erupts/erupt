package xyz.erupt.flow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.flow.bean.entity.OaTask;
import xyz.erupt.flow.bean.entity.OaTaskOperation;
import xyz.erupt.flow.mapper.OaTaskOperationMapper;
import xyz.erupt.flow.service.TaskOperationService;
import xyz.erupt.upms.model.EruptUser;
import xyz.erupt.upms.service.EruptUserService;

import java.util.Date;
import java.util.List;

@Service
public class TaskOperationServiceImpl extends ServiceImpl<OaTaskOperationMapper, OaTaskOperation>
        implements TaskOperationService, DataProxy<OaTaskOperation> {

    @Autowired
    private EruptUserService eruptUserService;

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
        this.save(build);
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
        this.save(build);
    }

    @Override
    public List<OaTaskOperation> listByOperator(String account) {
        return this.list(new LambdaQueryWrapper<OaTaskOperation>()
                .eq(OaTaskOperation::getOperator, account)
                .orderByDesc(OaTaskOperation::getOperationDate)
        );
    }
}

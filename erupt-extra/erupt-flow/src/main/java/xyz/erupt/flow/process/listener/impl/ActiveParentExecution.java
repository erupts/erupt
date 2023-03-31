package xyz.erupt.flow.process.listener.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.process.listener.AfterFinishExecutionListener;
import xyz.erupt.flow.service.ProcessExecutionService;
import xyz.erupt.flow.service.ProcessInstanceService;

/**
 * 线程结束后，尝试激活父线程
 * 如果没有父线程，则调用实例的结束
 */
@Component
public class ActiveParentExecution implements AfterFinishExecutionListener {

    @Override
    public int sort() {
        return 0;
    }

    @Autowired
    private ProcessExecutionService processExecutionService;
    @Autowired
    private ProcessInstanceService processInstanceService;

    @Override
    public void execute(OaProcessExecution executableNode) {
        if(executableNode.getParentId()!=null) {
            processExecutionService.active(executableNode.getParentId());
        }else {
            processInstanceService.finish(executableNode.getProcessInstId());
        }
    }
}

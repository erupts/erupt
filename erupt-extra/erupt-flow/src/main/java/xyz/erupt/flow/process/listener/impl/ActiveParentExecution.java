package xyz.erupt.flow.process.listener.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.process.listener.AfterFinishExecutionListener;
import xyz.erupt.flow.service.ProcessExecutionService;

/**
 * 线程结束后，尝试激活父线程
 */
@Component
public class ActiveParentExecution implements AfterFinishExecutionListener {

    @Autowired
    private ProcessExecutionService processExecutionService;

    @Override
    public void execute(OaProcessExecution executableNode) {
        if(executableNode.getParentId()!=null) {
            processExecutionService.active(executableNode.getParentId());
        }
    }
}

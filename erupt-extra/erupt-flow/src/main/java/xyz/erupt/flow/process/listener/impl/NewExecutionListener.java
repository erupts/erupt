package xyz.erupt.flow.process.listener.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessInstance;
import xyz.erupt.flow.process.listener.AfterCreateInstanceListener;
import xyz.erupt.flow.service.ProcessExecutionService;

/**
 * 启动实例，为当前实例创建线程
 */
@Component
public class NewExecutionListener implements AfterCreateInstanceListener {

    @Autowired
    private ProcessExecutionService processExecutionService;

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public void execute(OaProcessInstance inst) {
        processExecutionService.newExecutionForInstance(inst);
    }
}

package xyz.erupt.flow.process.listener.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessActivity;
import xyz.erupt.flow.process.listener.AfterFinishActivityListener;
import xyz.erupt.flow.service.ProcessActivityService;
import xyz.erupt.flow.service.ProcessExecutionService;

@Component
public class ActiveNextActivity implements AfterFinishActivityListener {

    @Override
    public int sort() {
        return 0;
    }

    @Autowired
    private ProcessActivityService processActivityService;
    @Autowired
    private ProcessExecutionService processExecutionService;

    @Override
    public void execute(OaProcessActivity executableNode) {
        //尝试激活下一个活动
        if(processActivityService.activeByExecutionId(executableNode.getExecutionId())) {
            return;
        }
        //没有下一个活动，表示当前活动完成，触发线程的步进
        processExecutionService.step(executableNode.getExecutionId(), executableNode.getProcessNode());
    }
}

package xyz.erupt.flow.process.listener.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.process.listener.AfterCreateExecutionListener;
import xyz.erupt.flow.service.ProcessActivityService;

/**
 * 启动线程后，为当前线程创建活动
 */
@Component
public class NewActivityListener implements AfterCreateExecutionListener {

    @Override
    public int sort() {
        return 0;
    }

    @Autowired
    private ProcessActivityService processActivityService;

    @Override
    public void execute(OaProcessExecution executableNode) {
        processActivityService.newActivitiesForExecution(executableNode);
    }
}

package xyz.erupt.flow.process.listener.impl;

import org.springframework.stereotype.Component;
import xyz.erupt.flow.bean.entity.OaProcessExecution;
import xyz.erupt.flow.process.listener.AfterFinishExecutionListener;

/**
 * 线程结束后置事件
 * 如果还有父线程，则激活父线程
 * 否则调用流程实例的结束
 */
@Component
public class NextExecution implements AfterFinishExecutionListener {

    @Override
    public int sort() {
        return 0;
    }

    @Override
    public void execute(OaProcessExecution execution) {
    }
}
